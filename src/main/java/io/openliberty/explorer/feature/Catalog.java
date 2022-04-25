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

import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Catalog {
    final Path featureSubdir;
    final Map<String, Feature> featureMap = new HashMap<>();
    final Map<String, Feature> shortNames = new HashMap<>();
    final Feature[] features;
    final Map<Feature, Integer> featureIndex = new HashMap<>();
    final BitSet[] dependencyMatrix;
    public Catalog(Path libertyRoot) {
        boolean ignoreDuplicates = true;
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
        // sort the features by full name
        this.features = featureMap.values().stream().sorted().toArray(Feature[]::new);
        // create a reverse look-up table for the array
        for (int i = 0; i < features.length; i++) featureIndex.put(features[i], i);
        // create an initially empty dependency matrix
        this.dependencyMatrix = Stream.generate(() -> new BitSet(features.length)).limit(features.length).toArray(BitSet[]::new);
        // add the dependencies
        allFeatures()
                .filter(Feature::hasContent)
                .forEach(f -> {
                    BitSet dependencies = dependencyMatrix[featureIndex.get(f)];
                    f.containedFeatures()
                            .map(featureMap::get)
                            .map(featureIndex::get)
                            .filter(Objects::nonNull) // ignore unknown features TODO: try tolerated versions instead
                            .forEach(dependencies::set);
                });
    }

//    void warnMissingFeatures() {
//        allFeatures()
//                .filter(Key.SUBSYSTEM_CONTENT)
//                .sorted(comparing(Main::fullName))
//                .forEach(f -> Key.SUBSYSTEM_CONTENT.parseValues(f)
//                        .filter(v -> "osgi.subsystem.feature".equals(v.getQualifier("type")))
//                        .map(v -> v.id)
//                        .filter(id -> !featureMap.containsKey(id))
//                        .forEach(id -> System.err.printf("WARNING: feature '%s' depends on absent feature '%s'. " +
//                                "This dependency will be ignored.%n", Main.fullName(f), id)));
//    }

    public Stream<Feature> allFeatures() {
        return Arrays.stream(features);
    }

    public Stream<Feature> dependencies(Feature rootFeature) {
        return rootFeature.containedFeatures().map(featureMap::get).filter(Objects::nonNull);
    }

    public void dfs(Feature rootFeature, Consumer<Feature> action) {
        dependencies(rootFeature).forEach(f -> dfs(f, action));
        action.accept(rootFeature);
    }
}
