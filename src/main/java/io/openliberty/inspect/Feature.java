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

import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Feature implements Element {
    private final String fullName;
    private final String shortName;
    private final String name;
    private final Visibility visibility;
    private final List<String> containedFeatures;
    private final boolean isAutoFeature;

    Feature(Path p) {
        final Attributes attributes;
        try (InputStream in = new FileInputStream(p.toFile())) {
            attributes = new Manifest(in).getMainAttributes();
        } catch (IOException e) {
            throw new IOError(e);
        }
        Optional<ValueElement> symbolicName = Key.SUBSYSTEM_SYMBOLICNAME.parseValues(attributes).findFirst();
        this.fullName = symbolicName.orElseThrow(Error::new).id;
        this.shortName = Key.IBM_SHORTNAME.get(attributes).orElse(null);
        this.visibility = symbolicName
                .map(v -> v.getQualifier("visibility"))
                .map(String::toUpperCase)
                .map(Visibility::valueOf)
                .orElse(Visibility.UNKNOWN);
        this.name = visibility == Visibility.PUBLIC ? shortName().orElse(fullName) : fullName;
        this.containedFeatures = Key.SUBSYSTEM_CONTENT.parseValues(attributes)
                .filter(v -> "osgi.subsystem.feature".equals(v.getQualifier("type")))
                .map(v -> v.id)
                .collect(Collectors.toUnmodifiableList());
        this.isAutoFeature = Key.IBM_PROVISION_CAPABILITY.isPresent(attributes);
    }

    @Override
    public String fullName() { return fullName; }
    @Override
    public Optional<String> shortName() { return Optional.ofNullable(shortName); }
    @Override
    public Visibility visibility() { return this.visibility; }
    @Override
    public String name() { return name; }
    @Override
    public Stream<String> containedElements() { return containedFeatures.stream(); }

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
    public String toString() { return fullName(); }
}
