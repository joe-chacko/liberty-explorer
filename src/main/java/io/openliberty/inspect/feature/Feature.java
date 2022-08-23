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

import io.openliberty.inspect.Element;
import io.openliberty.inspect.Visibility;
import org.osgi.framework.Version;

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

public final class Feature implements Element {
    private final String fullName;
    private final String shortName;
    private final String name;
    private final Version version;
    private final Visibility visibility;
    private final List<ContentSpec> contents;
    private final boolean isAutoFeature;

    public Feature(Path p) {
        final Attributes attributes;
        try (InputStream in = new FileInputStream(p.toFile())) {
            attributes = new Manifest(in).getMainAttributes();
        } catch (IOException e) {
            throw new IOError(e);
        }
        Optional<ManifestValueEntry> symbolicName = ManifestKey.SUBSYSTEM_SYMBOLICNAME.parseValues(attributes).findFirst();
        this.fullName = symbolicName.orElseThrow(Error::new).id;
        this.shortName = ManifestKey.IBM_SHORTNAME.get(attributes).orElse(null);
        this.visibility = symbolicName
                .map(v -> v.getQualifier("visibility"))
                .map(String::toUpperCase)
                .map(Visibility::valueOf)
                .orElse(Visibility.UNKNOWN);
        this.name = visibility == Visibility.PUBLIC ? shortName().orElse(fullName) : fullName;
        this.contents = ManifestKey.SUBSYSTEM_CONTENT.parseValues(attributes)
                .map(Feature::createSpec)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toUnmodifiableList());
        this.isAutoFeature = ManifestKey.IBM_PROVISION_CAPABILITY.isPresent(attributes);
        this.version = ManifestKey.SUBSYSTEM_VERSION.get(attributes).map(Version::new).orElse(Version.emptyVersion);
    }

    @Override
    public String symbolicName() { return fullName; }
    public Optional<String> shortName() { return Optional.ofNullable(shortName); }
    @Override
    public Visibility visibility() { return this.visibility; }
    @Override
    public String name() { return name; }
    @Override
    public Version version() { return version; }
    @Override
    public Stream<String> aka() { return Stream.of(shortName); }
    @Override
    public boolean isAutoFeature() { return isAutoFeature; }

    @Override
    public Stream<Element> findDependencies(Collection<Element> elements) {
        return contents.stream()
                .map(spec -> spec.findBestMatch(elements))
                .flatMap(Optional::stream);
    }

    @Override
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
        if (! (other instanceof Feature)) return false;
        Feature that = (Feature) other;
        return this.fullName.equals(that.fullName);
    }

    @Override
    public int hashCode() { return Objects.hash(fullName); }

    @Override
    public String toString() { return symbolicName(); }

    static Optional<ContentSpec> createSpec(ManifestValueEntry ve) {
        String type = ve.getQualifierOrDefault("type", "bundle");
        switch (type) {
            case "osgi.subsystem.feature": return Optional.of(ve).map(FeatureSpec::new);
            case "bundle": return Optional.of(ve).map(BundleSpec::new);
            case "file": return Optional.empty();
            case "jar": return Optional.empty();
            default: throw new IllegalStateException("Unknown content type: " + type);
        }
    }
}
