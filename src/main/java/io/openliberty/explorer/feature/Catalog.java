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

package io.openliberty.explorer.feature;

import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.nio.dot.DOTExporter;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toSet;
import static org.jgrapht.Graphs.addEdgeWithVertices;

public class Catalog {
    final Path featureSubdir;
    final Map<String, Feature> featureMap = new HashMap<>();
    final Map<String, Feature> shortNames = new HashMap<>();
    final SimpleDirectedGraph<Feature, DefaultEdge> dependencies = new SimpleDirectedGraph<>(DefaultEdge.class);
    public Catalog(Path libertyRoot) {
        this.featureSubdir = libertyRoot.resolve("lib/features");
        // validate directories
        if (!Files.isDirectory(libertyRoot))
            throw new Error("Not a valid directory: " + libertyRoot.toFile().getAbsolutePath());
        if (!Files.isDirectory(featureSubdir))
            throw new Error("No feature subdirectory found: " + featureSubdir.toFile().getAbsolutePath());
        // parse feature manifests
        try (var paths = Files.list(featureSubdir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".mf"))
                    .map(Feature::new)
                    .forEach(f -> {
                        var oldValue = featureMap.put(f.fullName(), f);
                        if (null != oldValue)
                            System.err.println("WARNING: duplicate symbolic name found: " + f.fullName());
                        f.shortName()
                                .map(shortName -> shortNames.put(shortName, f))
                                .ifPresent(shortName -> System.err.println("WARNING: duplicate short name found: " + shortName));
                    });
        } catch (IOException e) {
            throw new IOError(e);
        }
        // add the features and their dependencies to the graph
        featureMap.values().forEach(f1 -> f1.containedFeatures()
                .map(featureMap::get)
                .filter(Objects::nonNull) // ignore unknown features TODO: try tolerated versions instead
                .forEach(f2 -> addEdgeWithVertices(dependencies, f1, f2)));
    }

    public Stream<Feature> dependencies(Feature rootFeature) {
        return rootFeature.containedFeatures().map(featureMap::get).filter(Objects::nonNull);
    }

    public void dfs(Feature rootFeature, Consumer<Feature> action) {
        dependencies(rootFeature).forEach(f -> dfs(f, action));
        action.accept(rootFeature);
    }

    public void doSomething() {
        dependencies.vertexSet()
                .stream()
                .filter(Visibility.PUBLIC)
                .filter(f -> dependencies.inDegreeOf(f) == 0)
                .sorted()
                .forEach(System.out::println);
    }

    public Stream<Feature> findFeature(String pattern) {
        return dependencies.vertexSet().stream().filter(f -> f.matches(pattern));
    }

    public String generateSubgraph(String...patterns) {
        // convert the patterns to the matching features (ignoring duplicates)
        final Set<Feature> features = stream(patterns).flatMap(this::findFeature).collect(toSet());
        // start with the initial set of features
        Set<Feature> deps = features;
        while (!deps.isEmpty()) {
            deps = deps.stream()
                    // find the next level dependencies
                    .map(dependencies::outgoingEdgesOf)
                    .flatMap(Set::stream)
                    .map(dependencies::getEdgeTarget)
                    // filter out any that we already know about
                    .filter(not(features::contains))
                    .collect(toSet());
            features.addAll(deps);
        }
        var subgraph = new AsSubgraph<>(dependencies, features);
        var exporter = new DOTExporter<Feature, DefaultEdge>(f -> '"' + f.simpleName() + '"');
        var writer = new StringWriter();
        exporter.exportGraph(subgraph, writer);
        return writer.toString();
    }
}
