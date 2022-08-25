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
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;

public interface Element extends Comparable<Element> {
    Path path();
    String symbolicName();
    default String fileName() { return path().getFileName().toString(); }
    default String pathName() { return path().toString(); }
    String name();
    Version version();
    /** Returns a stream of other names for this element */
    Stream<String> aka();
    /** Returns a stream of <em>all</em> the names for this element */
    default Stream<String> allNames() {
        return concat(Stream.of(symbolicName(), name()), aka())
                .filter(Objects::nonNull)
                .distinct();
    }
    default Visibility visibility() { return Visibility.PRIVATE; }
    default boolean isAutoFeature() { return false; }

    default String simpleName() {
        return name()
                .replaceFirst("^com.ibm.websphere.app(server|client).", "")
                .replaceFirst("^com.ibm.websphere.", "")
                .replaceFirst("^(com.ibm.ws|io.openliberty).(com\\.|org\\.|net\\.)?", "")
                .replaceFirst("^io.openliberty.", "")
                .replaceAll("(\\D)\\.", "$1 ")
                .replaceAll("\\.(\\D)", " $1")
                .replaceAll("_", " ");
    }

    default Stream<Element> findDependencies(Collection<Element> elements) { return Stream.empty(); }
}
