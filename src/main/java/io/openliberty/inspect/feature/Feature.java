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
package io.openliberty.inspect.feature;

import static io.openliberty.inspect.Visibility.PUBLIC;
import static io.openliberty.inspect.Visibility.UNKNOWN;
import static io.openliberty.inspect.feature.ManifestKey.IBM_PROVISION_CAPABILITY;
import static io.openliberty.inspect.feature.ManifestKey.IBM_SHORTNAME;
import static io.openliberty.inspect.feature.ManifestKey.SUBSYSTEM_CONTENT;
import static io.openliberty.inspect.feature.ManifestKey.SUBSYSTEM_DESCRIPTION;
import static io.openliberty.inspect.feature.ManifestKey.SUBSYSTEM_SYMBOLICNAME;
import static io.openliberty.inspect.feature.ManifestKey.SUBSYSTEM_VERSION;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.osgi.framework.Version;

import io.openliberty.inspect.Element;
import io.openliberty.inspect.Visibility;

public final class Feature implements Element {
    private static final Pattern LDAP_FEATURE_IDS = Pattern.compile("(?<=osgi.identity=)(.*?)(?=\\))");
    private final Path path;
    private final String fullName;
    private final String shortName;
    private final String name;
    private final Version version;
    private final Visibility visibility;
    private final List<ContentSpec> contents;
    private final Manifest manifest;
    private final boolean isAutoFeature;
    private final List<List<String>> autoFeatureDetails;

    private final String desc;

    public Feature(Path path) {
        this.path = path.normalize();
        final Attributes attributes;
        try (InputStream in = new FileInputStream(path.toFile())) {
            attributes = new Manifest(in).getMainAttributes();
        } catch (IOException e) {
            throw new IOError(e);
        }
        Optional<ManifestValueEntry> symbolicName = SUBSYSTEM_SYMBOLICNAME.parseValues(attributes).findFirst();
        this.fullName = symbolicName.orElseThrow(Error::new).id;
        this.shortName = IBM_SHORTNAME.get(attributes).orElse(null);
        this.visibility = symbolicName.map(Feature::getVisibility).orElse(UNKNOWN);
        this.name = visibility == PUBLIC ? shortName().orElse(fullName) : fullName;
        this.contents = SUBSYSTEM_CONTENT.parseValues(attributes)
                .map(Feature::createSpec)
                .filter(Objects::nonNull)
                .collect(toUnmodifiableList());
        this.isAutoFeature = IBM_PROVISION_CAPABILITY.isPresent(attributes);
        this.autoFeatureDetails = IBM_PROVISION_CAPABILITY.parseValues(attributes)
                .map(ve -> ve.getQualifier("filter"))
                .map(this::parseFeaturesFromLdapExpression)
                .toList();
        this.version = SUBSYSTEM_VERSION.get(attributes).map(Version::new).orElse(Version.emptyVersion);
        try {
            this.manifest = new Manifest(path.toUri().toURL().openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.desc = SUBSYSTEM_DESCRIPTION.get(attributes)
                .map(this::resolveDescription)
                .orElseGet(this::getPrivateFeatureDescription);
    }

    private List<String> parseFeaturesFromLdapExpression(String ldapExpr) {
        return LDAP_FEATURE_IDS.matcher(ldapExpr).results()
                .map(MatchResult::group)
                .toList();
    }

    public List<List<String>> getAutoFeatureDetails() {
        return autoFeatureDetails;
    }


    private static Visibility getVisibility(ManifestValueEntry symbolicName) {
        String vis = symbolicName.getQualifier("visibility").toUpperCase();
        try {
            return Visibility.valueOf(vis);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Path path() { return path; }
    public String symbolicName() { return fullName; }
    public Optional<String> shortName() { return Optional.ofNullable(shortName); }
    public Visibility visibility() { return this.visibility; }
    public String name() { return name; }
    public String description() { return this.desc; }
    private String resolveDescription(String desc) {
        if (! desc.contains("%description")) return desc;
        try {
            return getPublicFeatureDescription();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Version version() { return version; }
    public Stream<String> aka() { return Stream.of(shortName); }
    public boolean isAutoFeature() { return isAutoFeature; }

    public Stream<Element> findDependencies(Collection<Element> elements) {
        return contents.stream()
                .map(spec -> spec.findBestMatch(elements))
                .flatMap(Optional::stream);
    }

    public boolean hasFeatureDependencies() { return contents.stream().filter(FeatureSpec.class::isInstance).findAny().isPresent(); }
    public boolean hasBundleDependencies() { return contents.stream().filter(BundleSpec.class::isInstance).findAny().isPresent(); }

    public Stream<String> getPrimaryFeatureDependencies() {
        return contents.stream()
                .filter(FeatureSpec.class::isInstance)
                .map(FeatureSpec.class::cast)
                .map(FeatureSpec::getPrimaryDependencyName);
    }

    public Stream<String> getToleratedFeatureDependencies() {
        return contents.stream()
                .filter(FeatureSpec.class::isInstance)
                .map(FeatureSpec.class::cast)
                .flatMap(FeatureSpec::getToleratedDependencyNames);
    }

    public Stream<String> formatFeatureDependencies() {
        return contents.stream()
                .filter(FeatureSpec.class::isInstance)
                .map(FeatureSpec.class::cast)
                .map(FeatureSpec::describe);
    }

    public Stream<String> formatBundleDependencies() {
        return contents.stream()
                .filter(BundleSpec.class::isInstance)
                .map(BundleSpec.class::cast)
                .map(BundleSpec::describe);
    }

    public int compareTo(Element other) {
        if (!(other instanceof Feature)) return -1; // Features sort before other element types
        Feature that = (Feature) other;
        int result = Boolean.compare(this.isAutoFeature, that.isAutoFeature);
        if (0 == result) result = this.visibility().compareTo(that.visibility());
        if (0 == result) result = this.name().compareTo(that.name());
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof Feature)) return false;
        Feature that = (Feature) other;
        return this.fullName.equals(that.fullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName);
    }

    @Override
    public String toString() {
        return symbolicName();
    }

    static ContentSpec createSpec(ManifestValueEntry ve) {
        String type = ve.getQualifierOrDefault("type", "bundle");
        switch (type) {
            case "osgi.subsystem.feature":
                return new FeatureSpec(ve);
            case "bundle":
                return new BundleSpec(ve);
            case "boot.jar":
            case "file":
            case "jar":
                return null;
            default:
                throw new IllegalStateException("Unknown content type: " + type);
        }
    }

    private String getPublicFeatureDescription() throws IOException {
        Path featuresRoot = path.getParent();
        Path propsFile = featuresRoot.resolve("l10n/" + symbolicName() + ".properties");
        if (!Files.exists(propsFile)) return "Feature description missing";
        Path propertiesFile = validate(propsFile);
        Properties prop = new Properties();
        prop.load(new FileInputStream(propertiesFile.toString()));
        return prop.getProperty("description");
    }

    private String getPrivateFeatureDescription() {
        Attributes attributes = manifest.getMainAttributes();
        String symbolicNameAttr = attributes.getValue("Subsystem-SymbolicName").substring(symbolicName().length() + 2);
        if(isAutoFeature()) return "" + symbolicNameAttr + "\n" + attributes.getValue("IBM-Provision-Capability");
        if(!symbolicNameAttr.isEmpty()) return symbolicNameAttr;
        return "";
    }

    private static Path validate(Path path) {
        if (Files.exists(path)) return path;
        throw new Error("No properties file found: " + path.toFile().getAbsolutePath());
    }
}
