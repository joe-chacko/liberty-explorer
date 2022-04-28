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
import io.openliberty.inspect.Feature;
import io.openliberty.inspect.Visibility;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsGraphUnion;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.openliberty.explore.Main.Direction.DOWN;
import static io.openliberty.explore.Main.Direction.UP;
import static io.openliberty.inspect.Visibility.PRIVATE;
import static io.openliberty.inspect.Visibility.PROTECTED;
import static io.openliberty.inspect.Visibility.PUBLIC;
import static io.openliberty.inspect.Visibility.UNKNOWN;
import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.jgrapht.nio.DefaultAttribute.createAttribute;

public class Main {


    public static final Attribute SUBJECT_FILL_COLOR = createAttribute("gray95");

    public static void main(String[] args) {
        try {
            Main main = new Main("/Users/chackoj/wlp", args);
            System.out.println(main.generateSubgraph());
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println(t);
        }
    }

    final Catalog liberty;
    final Set<Feature> subjectFeatures;

    Main(String libertyRoot, String...args) {
        this.liberty = new Catalog(Paths.get(libertyRoot));
        // convert the patterns to the matching features (ignoring duplicates)
        subjectFeatures = stream(args).flatMap(liberty::findFeature).collect(toUnmodifiableSet());
    }

    public String generateSubgraph() {
        var contains = findTransitivelyRelatedFeatures(subjectFeatures, DOWN);
        var containedBy = findTransitivelyRelatedFeatures(subjectFeatures, UP);
        Graph<Feature, DefaultEdge> g = liberty.dependencyGraph();
        var g1 = new AsSubgraph<>(g, contains);
        var g2 = new AsSubgraph<>(g, containedBy);
        var g3 = new AsGraphUnion<>(g1, g2);
        var exporter = new DOTExporter<Feature, DefaultEdge>(f -> '"' + f.simpleName() + '"');
        exporter.setVertexAttributeProvider(this::getDotAttributes);
        var writer = new StringWriter();
        exporter.exportGraph(g3, writer);
        return writer.toString();
    }

    private static final Map<Visibility, Attribute> SHAPE_FOR_VISIBILITY = unmodifiableMap(new EnumMap<>(Map.of(
            PUBLIC, createAttribute("tripleoctagon"),
            PROTECTED, createAttribute("doubleoctagon"),
            PRIVATE, createAttribute("octagon"),
            UNKNOWN, createAttribute("egg")
    )));

    private Map<String, Attribute> getDotAttributes(Feature feature) {
        Map<String, Attribute> result = new HashMap<>();
        result.put("shape", SHAPE_FOR_VISIBILITY.get(feature.visibility()));
        if (subjectFeatures.contains(feature)) {
            result.put("bgcolor", SUBJECT_FILL_COLOR);
            result.put("style", createAttribute("filled"));
        }
        return result;
    }

    enum Direction {DOWN, UP}

    private Set<Feature> findTransitivelyRelatedFeatures(Set<Feature> features, Direction direction) {
        // start with the initial set of features
        Set<Feature> results = new HashSet<>(features);
        Set<Feature> deps = results;
        Graph<Feature, DefaultEdge> graph = direction == DOWN ? liberty.dependencyGraph() : new EdgeReversedGraph<>(liberty.dependencyGraph());
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
