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

import org.osgi.framework.Version;

import java.nio.file.Path;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import static org.osgi.framework.Constants.BUNDLE_NAME;
import static org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME;
import static org.osgi.framework.Constants.BUNDLE_VERSION;

public final class Bundle implements Element {
    private final Path path;
    private final JarFile jar;
    private final Manifest manifest;
    private final Attributes attributes;
    private final String symbolicName;
    private final String name;
    private final Version version;

    Bundle(Path path) {
        this.path = path;
        try {
            this.jar = new JarFile(path.toFile());
            this.manifest = jar.getManifest();
            this.attributes = manifest.getMainAttributes();
            this.symbolicName = attributes.getValue(BUNDLE_SYMBOLICNAME).replaceFirst(";.*","");
            this.name = attributes.getValue(BUNDLE_NAME);
            this.version = Version.parseVersion(attributes.getValue(BUNDLE_VERSION));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    public String symbolicName() {
        return symbolicName;
    }
    @Override
    public String name() {
        return symbolicName() + "_" + version;
    }
    @Override
    public Version version() { return version; }
    public String fileName() { return path.getFileName().toString();}
    @Override
    public Stream<String> aka() { return Stream.of(fileName()); }
    @Override
    public int compareTo(Element o) { return o instanceof Bundle ? compareTo((Bundle) o) : 1;}

    private int compareTo(Bundle that) {
        return Optional.of(this.symbolicName.compareTo(that.symbolicName))
                .filter(i -> 0 != i)
                .orElseGet(() -> this.version.compareTo(that.version));
    }

    @Override
    public String toString() { return path.getFileName().toString(); }
}
