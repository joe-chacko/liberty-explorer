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

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.jgrapht.Graphs.addEdgeWithVertices;

public class Catalog {
    final Path featureSubdir;
    final Map<String, Feature> featureMap = new HashMap<>();
    final Map<String, Feature> shortNames = new HashMap<>();
    final SimpleDirectedGraph<Feature, DefaultEdge> dependencies = new SimpleDirectedGraph<>(DefaultEdge.class);
    public Catalog(Path libertyRoot) {
        boolean ignoreDuplicates = false;
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
                        if (null != oldValue && !ignoreDuplicates)
                            System.err.println("WARNING: duplicate symbolic name found: " + f.fullName());
                        f.shortName()
                                .map(shortName -> shortNames.put(shortName, f))
                                .filter(whatever -> !ignoreDuplicates)
                                .ifPresent(shortName -> System.err.println("WARNING: duplicate short name found: " + shortName));
                    });
        } catch (IOException e) {
            throw new IOError(e);
        }
        // add the features and their dependencies to the graph
        featureMap.values().stream()
                .forEach(f1 -> {
                    f1.containedFeatures()
                            .map(featureMap::get)
                            .filter(Objects::nonNull) // ignore unknown features TODO: try tolerated versions instead
                            .forEach(f2 -> addEdgeWithVertices(dependencies, f1, f2));
                });
    }

    public Stream<Feature> allFeatures() {
        return dependencies.vertexSet().stream().sorted();
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
                .filter(f -> dependencies.inDegreeOf(f) == 0)
                .sorted()
                .forEach(System.out::println);
    }
}
