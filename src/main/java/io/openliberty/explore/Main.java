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
import io.openliberty.inspect.Visibility;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.openliberty.explore.Main.Direction.FORWARD;
import static io.openliberty.explore.Main.Direction.REVERSE;
import static io.openliberty.inspect.Visibility.PRIVATE;
import static io.openliberty.inspect.Visibility.PROTECTED;
import static io.openliberty.inspect.Visibility.PUBLIC;
import static io.openliberty.inspect.Visibility.UNKNOWN;
import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.jgrapht.nio.DefaultAttribute.createAttribute;

public class Main {
    public static final Attribute SUBJECT_FILL_COLOR = createAttribute("gray95");
    private static final boolean VERBOSE = true;
    private final Set<FeatureQuery> includers;

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    public static void main(String[] args) {
        try {
            Main main = new Main("/Users/chackoj/wlp", args);
            System.out.println(main.generateSubgraph());
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println(t);
        }
    }

    private final Catalog liberty;
    private final Set<Element> initialMatches;
    private final Set<Element> initialPlusPaths;

    Main(String libertyRoot, String...patterns) {
        this.liberty = new Catalog(Paths.get(libertyRoot));

        // first, process the exclude patterns
        if (VERBOSE) System.err.println("Exclude patterns:");
        var excluders = stream(patterns)
                .filter(pattern -> pattern.startsWith("!"))
                .map(s -> s.substring(1))
                .map(FeatureQuery::new)
                .peek(fq -> {if (VERBOSE) System.err.println("\t" + fq);} )
                .collect(toUnmodifiableSet());
        // find excluded set
        var excluded = excluders.stream()
                .map(FeatureQuery::allMatches)
                .flatMap(Set::stream)
                .collect(toUnmodifiableSet());
        if (VERBOSE) System.err.println("Excluding features: " + excluded);
        // remove excluded features from graph
        // note that this will also remove all the associated edges
        liberty.exclude(excluded);

        // excluded features have been cast out, so work with whatever is left
        if (VERBOSE) System.err.println("Include patterns:");
        includers = stream(patterns)
                .filter(not(s -> s.startsWith("!")))
                .map(FeatureQuery::new)
                .peek(fq -> {if (VERBOSE) System.err.println("\t" + fq);} )
                .collect(toUnmodifiableSet());

        // find the initial set of features (not including deps)
        this.initialMatches = includers.stream()
                .map(FeatureQuery::initialMatches)
                .flatMap(Set::stream)
                .collect(toUnmodifiableSet());

        // construct graph of initial features, including paths between them
        var allPaths = new AllDirectedPaths<>(liberty.dependencyGraph());
        initialPlusPaths = allPaths.getAllPaths(initialMatches, initialMatches, true, null)
                .stream()
                .map(GraphPath::getVertexList)
                .flatMap(List::stream)
                .collect(toUnmodifiableSet());

    }

    class FeatureQuery {
        private final boolean includeContained;
        private final boolean includeContainedBy;
        private final String pattern;
        private final Set<Element> initialMatches;
        private final Set<Element> contained;
        private final Set<Element> containedBy;

        FeatureQuery(final String pattern) {
            this.includeContainedBy = pattern.startsWith("**/");
            var begin = includeContainedBy ? 3 : 0;
            this.includeContained = pattern.endsWith("/**");
            var end = includeContained ? pattern.length() - 3 : pattern.length();
            this.pattern = pattern.substring(begin, end);
            this.initialMatches = liberty.findFeatures(this.pattern).collect(toUnmodifiableSet());
            this.contained = includeContained ? findConnectedEdges(initialMatches, FORWARD) : emptySet();
            this.containedBy = includeContainedBy ? findConnectedEdges(initialMatches, REVERSE) : emptySet();
        }

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

    public String generateSubgraph() {
        class Holder {
            Graph<Element, DefaultEdge> graph = new AsSubgraph<>(liberty.dependencyGraph(), initialPlusPaths);
            void add(Graph<Element, DefaultEdge> graphToAdd) { graph = new AsGraphUnion<>(graph, graphToAdd); }
            void add(Holder that) { add(that.graph); }
        }
        var g = includers.stream()
                .map(FeatureQuery::subgraph)
                .collect(Holder::new, Holder::add, Holder::add)
                .graph;

        var exporter = new DOTExporter<Element, DefaultEdge>(f -> '"' + f.simpleName() + '"');
        exporter.setVertexAttributeProvider(this::getDotAttributes);
        var writer = new StringWriter();
        exporter.exportGraph(g, writer);
        return writer.toString();
    }

    private static final Map<Visibility, Attribute> SHAPE_FOR_VISIBILITY = unmodifiableMap(new EnumMap<>(Map.of(
            PUBLIC, createAttribute("tripleoctagon"),
            PROTECTED, createAttribute("doubleoctagon"),
            PRIVATE, createAttribute("octagon"),
            UNKNOWN, createAttribute("egg")
    )));

    private Map<String, Attribute> getDotAttributes(Element feature) {
        Map<String, Attribute> result = new HashMap<>();
        result.put("shape", SHAPE_FOR_VISIBILITY.get(feature.visibility()));
        if (initialMatches.contains(feature)) {
            result.put("bgcolor", SUBJECT_FILL_COLOR);
            result.put("style", createAttribute("filled"));
        }
        return result;
    }

    enum Direction {FORWARD, REVERSE}

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
}
