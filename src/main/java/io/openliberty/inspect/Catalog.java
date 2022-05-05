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

package io.openliberty.inspect;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class Catalog {
    public static SimpleDirectedGraph<Element, DefaultEdge> newGraph() {
        return new SimpleDirectedGraph<>(DefaultEdge.class);
    }

    // Wrap (downcased) feature names and shortnames as Path objects
    // to allow use of java.nio.file.FileSystem's built-in glob matching
    private final Map<Path, Feature> featureIndex = new HashMap<>();
    private final SimpleDirectedGraph<Element, DefaultEdge> dependencies = newGraph();

    public Catalog(String libertyRoot) { this(validate(Paths.get(libertyRoot), "Not a valid directory: ")); }

    private Catalog(Path libertyRoot) {
        final Map<String, Feature> featureMap = new HashMap<>();
        Path libDir = validate(libertyRoot.resolve("lib"), "No lib subdirectory found: ");
        Path featureDir = validate(libertyRoot.resolve("lib/features"), "No feature subdirectory found: ");
        // validate directories
        // parse feature manifests
        try (var paths = Files.list(featureDir)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".mf"))
                    .map(Feature::new)
                    .forEach(f -> {
                        var oldValue = featureMap.put(f.fullName(), f);
                        if (null != oldValue)
                            System.err.printf("WARNING: duplicate short names found for features: '%s' and '%s'%n", f, oldValue);
                        featureIndex.put(Paths.get(f.fullName().toLowerCase()), f);
                        f.shortName()
                                .map(String::toLowerCase)
                                .map(Paths::get)
                                .map(p -> featureIndex.put(p, f))
                                .filter(not(f::equals)) // ignore if the duplicate name is for the same feature (i.e. it had the same symbolic and shortname)
                                .ifPresent(f2 -> System.err.printf("WARNING: duplicate short names found for features: '%s' and '%s'%n", f, f2));
                    });
        } catch (IOException e) {
            throw new IOError(e);
        }

        // add the features to the graph
        featureMap.values()
                .stream()
                .distinct()
                .forEach(dependencies::addVertex);
        // add the feature dependencies to the graph
        featureMap.values().forEach(f1 -> f1.containedElements()
                .map(featureMap::get)
                .filter(Objects::nonNull) // ignore unknown features TODO: try tolerated versions instead
                .forEach(f2 -> dependencies.addEdge(f1, f2)));
    }

    private static Path validate(Path path, String desc) {
        if (isDirectory(path)) return path;
        throw new Error(desc + path.toFile().getAbsolutePath());
    }

    public Stream<Feature> findFeatures(String pattern) {
        pattern = requireNonNull(pattern).toLowerCase();
        if (!pattern.contains(":")) pattern = "glob:" + pattern;
        return featureIndex.keySet().stream()
                .filter(FileSystems.getDefault().getPathMatcher(pattern)::matches)
                .map(featureIndex::get)
                .filter(dependencies::containsVertex)
                .sorted()
                .distinct();
    }

    public Graph<Element, DefaultEdge> dependencyGraph() { return new AsUnmodifiableGraph<>(dependencies); }

    public void exclude(Element excluded) {
        dependencies.removeVertex(excluded);
    }
}
