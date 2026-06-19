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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scans a Liberty runtime installation and builds a lightweight model suitable
 * for API and test fixture generation.
 */
public class LibertyRuntimeScanner {
    private static final Pattern LDAP_FEATURE_IDS = Pattern.compile("(?<=osgi.identity=)(.*?)(?=\\))");

    public LibertyModel scan(Path libertyRoot, String libertyVersion, String gradlePluginVersion) throws IOException {
        return scan(libertyRoot, null, libertyVersion, gradlePluginVersion);
    }

    public LibertyModel scan(Path libertyRoot, Path serverConfigDir, String libertyVersion, String gradlePluginVersion)
            throws IOException {
        Path normalizedRoot = libertyRoot.toAbsolutePath().normalize();
        Path libDir = requireDirectory(normalizedRoot.resolve("lib"));
        Path platformDir = requireDirectory(libDir.resolve("platform"));
        Path devDir = requireDirectory(normalizedRoot.resolve("dev"));
        Path featureDir = libDir.resolve("features");

        List<FeatureInfo> features = new ArrayList<>();
        features.addAll(scanFeatures(platformDir));
        if (Files.isDirectory(featureDir)) {
            features.addAll(scanFeatures(featureDir));
        }

        if (serverConfigDir != null) {
            Set<String> configuredFeatures = readConfiguredFeatures(serverConfigDir.resolve("server.xml"));
            features.removeIf(feature -> !shouldIncludeFeature(feature, configuredFeatures));
        }

        enrichFeatureRelationships(features);
        features.sort(Comparator.comparing(FeatureInfo::getSymbolicName));

        List<BundleInfo> bundles = scanBundles(libDir, devDir);
        bundles.sort(Comparator.comparing(BundleInfo::getSymbolicName));

        return new LibertyModel(libertyVersion, gradlePluginVersion, features, bundles);
    }

    private void enrichFeatureRelationships(List<FeatureInfo> features) {
        Map<String, FeatureInfo> bySymbolicName = new LinkedHashMap<>();
        Map<String, FeatureInfo> byName = new LinkedHashMap<>();

        for (FeatureInfo feature : features) {
            bySymbolicName.put(feature.getSymbolicName(), feature);
            byName.put(feature.getName(), feature);
            feature.setDependents(new ArrayList<>());
        }

        for (FeatureInfo feature : features) {
            for (String dependency : feature.getDependencies()) {
                FeatureInfo dependencyFeature = bySymbolicName.get(dependency);
                if (dependencyFeature == null) {
                    dependencyFeature = byName.get(shortNameFromSymbolicName(dependency));
                }
                if (dependencyFeature != null && !dependencyFeature.getDependents().contains(feature.getSymbolicName())) {
                    dependencyFeature.getDependents().add(feature.getSymbolicName());
                }
            }
            feature.getDependents().sort(String::compareTo);
        }
    }

    private boolean shouldIncludeFeature(FeatureInfo feature, Set<String> configuredFeatures) {
        if (configuredFeatures.isEmpty()) {
            return true;
        }
        if (configuredFeatures.contains(feature.getName()) || configuredFeatures.contains(feature.getSymbolicName())) {
            return true;
        }
        if (feature.getDependencies().stream().anyMatch(configuredFeatures::contains)) {
            return true;
        }
        if (feature.getDependencies().stream().map(this::shortNameFromSymbolicName).anyMatch(configuredFeatures::contains)) {
            return true;
        }
        return feature.getAutoFeatureConditions().stream()
                .flatMap(List::stream)
                .anyMatch(trigger -> configuredFeatures.contains(trigger) || configuredFeatures.contains(shortNameFromSymbolicName(trigger)));
    }

    private Set<String> readConfiguredFeatures(Path serverXml) throws IOException {
        if (!Files.exists(serverXml)) {
            return Set.of();
        }

        Set<String> configuredFeatures = new LinkedHashSet<>();
        for (String line : Files.readAllLines(serverXml)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("<feature>") && trimmed.endsWith("</feature>")) {
                configuredFeatures.add(trimmed.substring("<feature>".length(), trimmed.indexOf("</feature>")).trim());
            }
        }
        return configuredFeatures;
    }

    private List<FeatureInfo> scanFeatures(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".mf"))
                    .map(this::parseFeature)
                    .filter(feature -> feature.getSymbolicName() != null)
                    .sorted(Comparator.comparing(FeatureInfo::getSymbolicName))
                    .toList();
        }
    }

    private FeatureInfo parseFeature(Path manifestPath) {
        try (InputStream inputStream = Files.newInputStream(manifestPath)) {
            Attributes attributes = new Manifest(inputStream).getMainAttributes();

            String symbolicName = firstSegment(attributes.getValue("Subsystem-SymbolicName"));
            String shortName = attributes.getValue("IBM-ShortName");
            String visibility = extractDirective(attributes.getValue("Subsystem-SymbolicName"), "visibility");
            String version = attributes.getValue("Subsystem-Version");
            String description = resolveDescription(manifestPath, symbolicName, attributes.getValue("Subsystem-Description"));
            String provisionCapability = attributes.getValue("IBM-Provision-Capability");
            boolean autoFeature = provisionCapability != null;

            FeatureInfo feature = new FeatureInfo(symbolicName, shortName != null ? shortName : symbolicName, version);
            feature.setDescription(description);
            feature.setVisibility(visibility != null ? visibility.toUpperCase(Locale.ROOT) : "UNKNOWN");
            feature.setAutoFeature(autoFeature);
            feature.setDependencies(parseFeatureDependencies(attributes.getValue("Subsystem-Content")));
            feature.setAutoFeatureConditions(parseAutoFeatureConditions(provisionCapability));
            return feature;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to parse feature manifest " + manifestPath, e);
        }
    }

    private List<String> parseFeatureDependencies(String subsystemContent) {
        if (subsystemContent == null || subsystemContent.isBlank()) {
            return List.of();
        }

        List<String> dependencies = new ArrayList<>();
        for (String entry : subsystemContent.split(",")) {
            String trimmed = entry.trim();
            if (trimmed.contains("type=osgi.subsystem.feature")) {
                dependencies.add(firstSegment(trimmed));
            }
        }
        return dependencies;
    }

    private List<List<String>> parseAutoFeatureConditions(String provisionCapability) {
        if (provisionCapability == null || provisionCapability.isBlank()) {
            return List.of();
        }

        List<List<String>> conditions = new ArrayList<>();
        for (String clause : provisionCapability.split("\\),\\(")) {
            List<String> triggers = LDAP_FEATURE_IDS.matcher(clause).results()
                    .map(MatchResult::group)
                    .distinct()
                    .toList();
            if (!triggers.isEmpty()) {
                conditions.add(triggers);
            }
        }
        return conditions;
    }

    private List<BundleInfo> scanBundles(Path libDir, Path devDir) throws IOException {
        List<BundleInfo> bundles = new ArrayList<>();
        try (Stream<Path> libPaths = Files.list(libDir)) {
            libPaths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .map(this::parseBundle)
                    .filter(bundle -> bundle != null)
                    .forEach(bundles::add);
        }

        try (Stream<Path> devPaths = Files.walk(devDir)) {
            devPaths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .map(this::parseBundle)
                    .filter(bundle -> bundle != null)
                    .forEach(bundles::add);
        }

        return bundles;
    }

    private BundleInfo parseBundle(Path jarPath) {
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }

            Attributes attributes = manifest.getMainAttributes();
            String symbolicName = firstSegment(attributes.getValue("Bundle-SymbolicName"));
            String version = attributes.getValue("Bundle-Version");
            if (symbolicName == null || version == null) {
                return null;
            }

            BundleInfo bundle = new BundleInfo(symbolicName, symbolicName + "_" + version, version);
            bundle.setDescription(defaultString(attributes.getValue("Bundle-Description"), "No Description found"));
            bundle.setDependencies(List.of());
            return bundle;
        } catch (IOException e) {
            return null;
        }
    }

    private String resolveDescription(Path manifestPath, String symbolicName, String rawDescription) throws IOException {
        if (rawDescription == null) {
            return "";
        }
        if (!rawDescription.contains("%description")) {
            return rawDescription;
        }

        Path propertiesPath = manifestPath.getParent().resolve("l10n").resolve(symbolicName + ".properties");
        if (!Files.exists(propertiesPath)) {
            return "Feature description missing";
        }

        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(propertiesPath)) {
            properties.load(inputStream);
        }
        return properties.getProperty("description", "Feature description missing");
    }

    private Path requireDirectory(Path path) {
        if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Required Liberty directory not found: " + path);
        }
        return path;
    }

    private String firstSegment(String value) {
        if (value == null) {
            return null;
        }
        int separator = value.indexOf(';');
        return separator >= 0 ? value.substring(0, separator) : value;
    }

    private String extractDirective(String value, String directiveName) {
        if (value == null) {
            return null;
        }
        String prefix = directiveName + ":=";
        for (String segment : value.split(";")) {
            String trimmed = segment.trim();
            if (trimmed.startsWith(prefix)) {
                return trimmed.substring(prefix.length()).replace("\"", "");
            }
        }
        return null;
    }

    private String shortNameFromSymbolicName(String symbolicName) {
        if (symbolicName == null) {
            return null;
        }
        return symbolicName
                .replaceFirst("^io\\.openliberty\\.", "")
                .replaceFirst("^com\\.ibm\\.websphere\\.appserver\\.", "");
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}

// Made with Bob
