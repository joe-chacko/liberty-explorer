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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.openliberty.GraphCollectors.toUnionWith;
import static io.openliberty.explore.LibertyExplorer.Direction.FORWARD;
import static io.openliberty.explore.LibertyExplorer.Direction.REVERSE;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Collectors.toUnmodifiableSet;

@Command(
        name = "lx",
        description = "Liberty installation eXplorer",
        subcommands = {GraphCommand.class, HelpCommand.class},
        defaultValueProvider = PropertiesDefaultProvider.class
)
public class LibertyExplorer implements Callable<Integer> {

    public static final String INCLUDE_CONTAINED_BY_PREFIX = "**/";
    public static final String INCLUDE_CONTAINED_SUFFIX = "/**";

    public static void main(String[] args) {
        LibertyExplorer explorer = new LibertyExplorer();
        CommandLine commandLine = new CommandLine(explorer);
        commandLine.registerConverter(Catalog.class, Catalog::new);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Option(names = { "-d", "--directory"}, defaultValue = ".",
            description = "Liberty root directory (defaults to the working directory)")
    Catalog liberty;

    @Option(names = {"-v", "--verbose"})
    boolean verbose;

    List<Query> queries;

    private Set<Element> primaryMatches;
    private Set<Element> interpolatedMatches;
    private Graph<Element, DefaultEdge> subgraph;

    @Override
    public Integer call() {
        System.out.println("Hello, world.");
        return 0;
    }

    boolean isPrimary(Element e) { return primaryMatches.contains(e); }
    Graph<Element, DefaultEdge> subgraph() {return subgraph;}

    void init(List<String> patterns) {
        if (verbose) System.err.println("Patterns: " + patterns.stream().collect(Collectors.joining("' '", "'", "'")));
        // create query objects from patterns
        queries = patterns.stream()
                .map(Query::new)
                .collect(toUnmodifiableList());

        excludeElements();

        // work with remaining features
        if (verbose) System.err.println("Include patterns:");
        var includers = this.queries.stream()
                .filter(not(Query::isNegatory))
                .peek(q -> {if (verbose) System.err.println("\t" + q);})
                .collect(toUnmodifiableSet());

        // find the initial set of features (not including deps)
        primaryMatches = includers.stream()
                .map(Query::initialMatches)
                .flatMap(Set::stream)
                .collect(toUnmodifiableSet());

        // construct graph of initial features, including paths between them
        var allPaths = new AllDirectedPaths<>(liberty.dependencyGraph());
        interpolatedMatches = allPaths.getAllPaths(primaryMatches, primaryMatches, true, null)
                .stream()
                .map(GraphPath::getVertexList)
                .flatMap(List::stream)
                .collect(toUnmodifiableSet());

        subgraph = new AsSubgraph<>(liberty.dependencyGraph(), interpolatedMatches);

        subgraph = includers.stream()
                .map(Query::subgraph)
                .collect(toUnionWith(subgraph));
    }

    void excludeElements() {
        // remove excluded features (and associated edges) from graph
        if (verbose) System.err.println("Exclude patterns:");
        queries.stream()
                .filter(Query::isNegatory)
                .distinct()
                .peek(q -> {if (verbose) System.err.println("\t" + q);} )
                .map(Query::allMatches)
                .flatMap(Set::stream)
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
                    .collect(toSet());
            results.addAll(deps);
        }
        return unmodifiableSet(results);
    }

    enum Direction {FORWARD, REVERSE}

    private class Query {
        private final boolean isNegatory;
        private final boolean includeContained;
        private final boolean includeContainedBy;
        private final String pattern;
        private final Set<Element> initialMatches;
        private final Set<Element> contained;
        private final Set<Element> containedBy;

        Query(final String pattern) {
            this.isNegatory = pattern.startsWith("!");
            var begin = isNegatory ? 1 : 0;
            this.includeContainedBy = pattern.startsWith(INCLUDE_CONTAINED_BY_PREFIX, begin);
            if (includeContainedBy) begin += INCLUDE_CONTAINED_BY_PREFIX.length();
            this.includeContained = pattern.endsWith(INCLUDE_CONTAINED_SUFFIX);
            var end = includeContained ? pattern.length() - INCLUDE_CONTAINED_SUFFIX.length() : pattern.length();
            this.pattern = pattern.substring(begin, end);
            this.initialMatches = liberty.findMatches(this.pattern).collect(toUnmodifiableSet());
            this.contained = includeContained ? findConnectedEdges(initialMatches, FORWARD) : emptySet();
            this.containedBy = includeContainedBy ? findConnectedEdges(initialMatches, REVERSE) : emptySet();
        }

        boolean isNegatory() { return isNegatory; }

        Set<Element> initialMatches() { return initialMatches; }

        Set<Element> allMatches() {
            return Stream.of(initialMatches, contained, containedBy)
                    .flatMap(Set::stream)
                    .collect(toUnmodifiableSet());
        }

        Graph<Element, DefaultEdge> subgraph() {
            return new AsGraphUnion<>(
                    new AsSubgraph<>(liberty.dependencyGraph(), contained),
                    new AsSubgraph<>(liberty.dependencyGraph(), containedBy)
            );
        }

        @Override
        public String toString() {
            return (includeContainedBy ? "**/" : "") + pattern + (includeContained ? "/**" : "");
        }
    }
}
