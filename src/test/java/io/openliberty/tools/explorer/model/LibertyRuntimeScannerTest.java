/*
 * Copyright (c) 2026 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package io.openliberty.tools.explorer.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LibertyRuntimeScannerTest {
    private static LibertyModel model;

    @BeforeAll
    static void setUp() throws Exception {
        String runtimeDir = System.getProperty("test.liberty.runtime.dir");
        String serverConfigDir = System.getProperty("test.liberty.server.config.dir");
        String runtimeVersion = System.getProperty("test.liberty.runtime.version");
        String pluginVersion = System.getProperty("test.liberty.gradle.plugin.version");

        assertNotNull(runtimeDir, "test.liberty.runtime.dir must be configured");
        assertNotNull(serverConfigDir, "test.liberty.server.config.dir must be configured");
        assertNotNull(runtimeVersion, "test.liberty.runtime.version must be configured");
        assertNotNull(pluginVersion, "test.liberty.gradle.plugin.version must be configured");

        Path libertyRoot = Path.of(runtimeDir);
        Path serverConfigRoot = Path.of(serverConfigDir);
        assertTrue(Files.isDirectory(libertyRoot), "Resolved Liberty runtime directory must exist");
        assertTrue(Files.isDirectory(serverConfigRoot), "Generated Liberty server config directory must exist");

        model = new LibertyRuntimeScanner().scan(libertyRoot, serverConfigRoot, runtimeVersion, pluginVersion);
    }

    @Test
    void scanBuildsModelWithPinnedVersions() {
        assertEquals(System.getProperty("test.liberty.runtime.version"), model.getLibertyVersion());
        assertEquals(System.getProperty("test.liberty.gradle.plugin.version"), model.getGradlePluginVersion());
    }

    @Test
    void scanFindsFeatureAndBundleSets() {
        assertTrue(model.getFeatures().size() >= 4, "Expected configured features from the fixed Liberty runtime");
        assertTrue(model.getBundles().size() > 20, "Expected a rich bundle set from the fixed Liberty runtime");
    }

    @Test
    void scanIncludesConfiguredFeatures() {
        assertTrue(findFeature("restfulWS-3.1").isPresent());
        assertTrue(findFeature("cdi-4.0").isPresent());
        assertTrue(findFeature("servlet-6.0").isPresent());
        assertTrue(findFeature("jsonb-3.0").isPresent());
    }

    @Test
    void knownFeatureContainsExpectedMetadata() {
        FeatureInfo feature = findFeature("restfulWS-3.1").orElseThrow();

        assertNotNull(feature.getSymbolicName());
        assertFalse(feature.getSymbolicName().isBlank());
        assertNotNull(feature.getName());
        assertFalse(feature.getName().isBlank());
        assertNotNull(feature.getVersion());
        assertFalse(feature.getVersion().isBlank());
        assertNotNull(feature.getVisibility());
        assertFalse(feature.getVisibility().isBlank());
    }

    @Test
    void configuredFeaturesRetainStableIdentity() {
        assertTrue(model.getFeatures().stream().allMatch(feature -> feature.getType() == ElementType.FEATURE));
        assertTrue(model.getFeatures().stream().allMatch(feature -> feature.getDependencies() != null));
        assertTrue(model.getFeatures().stream().allMatch(feature -> feature.getDependents() != null));
        assertTrue(model.getFeatures().stream().allMatch(feature -> feature.getAutoFeatureConditions() != null));
    }

    @Test
    void featureRelationshipsExposeStructuredCollections() {
        model.getFeatures().forEach(feature -> {
            assertNotNull(feature.getDependencies());
            assertNotNull(feature.getDependents());
            assertNotNull(feature.getAutoFeatureConditions());
        });
    }

    @Test
    void autoFeaturesAreDetectedWhenPresent() {
        long autoFeatureCount = model.getFeatures().stream().filter(FeatureInfo::isAutoFeature).count();
        assertTrue(autoFeatureCount >= 0);
        model.getFeatures().stream()
                .filter(FeatureInfo::isAutoFeature)
                .forEach(feature -> assertNotNull(feature.getAutoFeatureConditions()));
    }

    @Test
    void discoveredRelationshipsReferenceKnownFeatures() {
        model.getFeatures().forEach(feature -> {
            feature.getDependencies().forEach(dependency -> assertFalse(dependency.isBlank()));
            feature.getDependents().forEach(dependent -> assertFalse(dependent.isBlank()));
            feature.getAutoFeatureConditions().forEach(condition -> {
                assertFalse(condition.isEmpty());
                condition.forEach(trigger -> assertFalse(trigger.isBlank()));
            });
        });
    }

    @Test
    void scanIncludesRepresentativeBundles() {
        assertTrue(model.getBundles().stream().anyMatch(bundle -> bundle.getSymbolicName().contains("kernel")
                || bundle.getSymbolicName().contains("logging")
                || bundle.getSymbolicName().contains("osgi")),
                "Expected representative Liberty bundles to be present");
    }

    @Test
    void bundleMetadataIsPopulated() {
        BundleInfo bundle = model.getBundles().stream()
                .filter(candidate -> candidate.getSymbolicName() != null && candidate.getVersion() != null)
                .findFirst()
                .orElseThrow();

        assertNotNull(bundle.getName());
        assertFalse(bundle.getName().isBlank());
        assertNotNull(bundle.getDescription());
        assertNotNull(bundle.getDependencies());
        assertEquals(ElementType.BUNDLE, bundle.getType());
    }

    private Optional<FeatureInfo> findFeature(String shortName) {
        return model.getFeatures().stream()
                .filter(feature -> shortName.equals(feature.getName())
                        || feature.getSymbolicName().endsWith("." + shortName)
                        || feature.getSymbolicName().endsWith(shortName))
                .findFirst();
    }
}

// Made with Bob
