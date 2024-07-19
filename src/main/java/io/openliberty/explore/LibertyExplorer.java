/*
 * =============================================================================
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * =============================================================================
 */
package io.openliberty.explore;


import io.openliberty.inspect.Catalog;
import io.openliberty.inspect.Element;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.openliberty.GraphCollectors.toUnionWith;
import static io.openliberty.explore.LibertyExplorer.Direction.FORWARD;
import static io.openliberty.explore.LibertyExplorer.Direction.REVERSE;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Command(
        name = "lx",
        description = "Liberty installation eXplorer",
        version = "Liberty installation eXplorer 0.5",
        subcommands = {DescribeCommand.class, ListCommand.class, GraphCommand.class, TreeCommand.class, HelpCommand.class},
        defaultValueProvider = PropertiesDefaultProvider.class
)
public class LibertyExplorer {
    public static final String INCLUDE_CONTAINED_BY_PREFIX = "**/";
    public static final String INCLUDE_CONTAINED_SUFFIX = "/**";
    private List<String> patterns;

    public static void main(String[] args) {
        LibertyExplorer explorer = new LibertyExplorer();
        CommandLine commandLine = new CommandLine(explorer);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Option(names = "--directory",
            defaultValue = ".",
            description = "Liberty root directory (defaults to the working directory)")
    Path libertyRoot;

    @Option(names = "--verbose",
            description = "Provide more detailed output")
    boolean verbose;

    @Option(names = "--bundles",
            description = "Process bundles in the dependency analysis")
    boolean includeBundles;

    Catalog liberty;
    private List<Query> queries;
    private Set<Element> primaryMatches;
    private Set<Element> interpolatedMatches;
    private Graph<Element, DefaultEdge> subgraph;

    boolean isPrimary(Element e) { return primaryResults().contains(e); }

    void init(List<String> patterns) throws Exception {
        liberty = new Catalog(libertyRoot, includeBundles);
        if (verbose) System.err.println("Patterns: " + patterns.stream().collect(Collectors.joining("' '", "'", "'")));
        this.patterns = patterns;
        removeExcludedElements();
    }

    void removeExcludedElements() {
        // remove excluded features (and associated edges) from graph
        if (verbose) System.err.println("Exclude patterns:");
        queries().stream()
                .filter(Query::isExcludeQuery)
                .distinct()
                .peek(q -> {if (verbose) System.err.println("\t" + q);} )
                .flatMap(Query::allMatches)
                .distinct()
                .peek(e -> {if (verbose) System.err.println("Excluding: " + e);})
                .forEach(liberty::exclude);
    }

    private Set<Element> findConnectedEdges(Set<Element> features, Direction direction) {
        // start with the initial set of features
        var results = new HashSet<>(features);
        var deps = unmodifiableSet(results);
        var graph = direction == FORWARD ? liberty.dependencyGraph() : new EdgeReversedGraph<>(liberty.dependencyGraph());
        while (!deps.isEmpty()) {
            deps = deps.stream()
                    // find the next level dependencies
                    .map(graph::outgoingEdgesOf)
                    .flatMap(Set::stream)
                    .map(graph::getEdgeTarget)
                    // filter out any that we already know about
                    .filter(not(results::contains))
                    .collect(toUnmodifiableSet());
            results.addAll(deps);
        }
        return unmodifiableSet(results);
    }

    private List<Query> queries() {
        requireNonNull(patterns,"Explorer not yet initialised with patterns.");
        if (null == queries) queries = patterns.stream().map(Query::new).collect(toUnmodifiableList());
        return queries;
    }

    Set<Element> primaryResults() {
        if (null == primaryMatches) {
            // find the initial set of elements (not including deps)
            if (verbose) System.err.println("Include patterns:");
            primaryMatches = queries().stream()
                    .filter(Query::isIncludeQuery)
                    .peek(q -> {if (verbose) System.err.println("\t" + q);})
                    .distinct()
                    .map(Query::initialMatches)
                    .flatMap(Set::stream)
                    .collect(toUnmodifiableSet());
        }
        return primaryMatches;
    }

    Set<Element> allResults() {
        return queries().stream()
                .filter(Query::isIncludeQuery)
                .distinct()
                .flatMap(Query::allMatches)
                .collect(Collectors.toUnmodifiableSet());
    }

    Set<Element> interpolatedResults() {
        if (null == interpolatedMatches) interpolatedMatches = new AllDirectedPaths<>(liberty.dependencyGraph())
                .getAllPaths(primaryResults(), primaryResults(), true, null)
                .stream()
                .map(GraphPath::getVertexList)
                .flatMap(List::stream)
                .collect(toUnmodifiableSet());
        return interpolatedMatches;
    }

    Graph<Element, DefaultEdge> subgraph() {
        if (null == subgraph) {
            subgraph = new AsSubgraph<>(liberty.dependencyGraph(), interpolatedResults());
            subgraph = queries().stream()
                    .filter(Query::isIncludeQuery)
                    .peek(q -> {if (verbose) System.err.println("\t" + q);})
                    .distinct()
                    .map(Query::subgraph)
                    .collect(toUnionWith(subgraph));
        }
        return subgraph;
    }

    enum Direction {FORWARD, REVERSE}

    private class Query {
        private final boolean isExcludeQuery;
        private final boolean includeContained;
        private final boolean includeContainedBy;
        private final String pattern;
        private Set<Element> initialMatches;
        private Set<Element> contained;
        private Set<Element> containedBy;

        Query(final String pattern) {
            this.isExcludeQuery = pattern.startsWith("!");
            var begin = isExcludeQuery ? 1 : 0;
            this.includeContainedBy = pattern.startsWith(INCLUDE_CONTAINED_BY_PREFIX, begin);
            if (includeContainedBy) begin += INCLUDE_CONTAINED_BY_PREFIX.length();
            this.includeContained = pattern.endsWith(INCLUDE_CONTAINED_SUFFIX);
            var end = includeContained ? pattern.length() - INCLUDE_CONTAINED_SUFFIX.length() : pattern.length();
            this.pattern = pattern.substring(begin, end);
        }

        boolean isExcludeQuery() { return isExcludeQuery; }
        boolean isIncludeQuery() { return !isExcludeQuery; }

        Set<Element> initialMatches() {
            if (null == initialMatches) initialMatches = liberty.findMatches(this.pattern).collect(toUnmodifiableSet());
            return initialMatches;
        }

        Stream<Element> allMatches() {
            return Stream.of(initialMatches(), contained(), containedBy())
                    .flatMap(Set::stream);
        }

        Graph<Element, DefaultEdge> subgraph() {
            return new AsGraphUnion<>(
                    new AsSubgraph<>(liberty.dependencyGraph(), contained()),
                    new AsSubgraph<>(liberty.dependencyGraph(), containedBy())
            );
        }

        @Override
        public String toString() {
            return (includeContainedBy ? "**/" : "") + pattern + (includeContained ? "/**" : "");
        }

        public Set<Element> contained() {
            if (null == contained) contained = includeContained ? findConnectedEdges(initialMatches, FORWARD) : emptySet();
            return contained;
        }

        public Set<Element> containedBy() {
            if (null == containedBy) containedBy = includeContainedBy ? findConnectedEdges(initialMatches, REVERSE) : emptySet();
            return containedBy;
        }
    }
}
