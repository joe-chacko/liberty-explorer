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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;

public class FeatureSpec implements ContentSpec {
    // first item is the preferred version, the rest are tolerated
    private final List<String> symbolicNames;

    public FeatureSpec(ManifestValueEntry ve) {
        String prefix = ve.id.replaceFirst("-[^-]*$", "-");
        var tolerated = ve.getQualifierIfPresent("ibm.tolerates")
                .map(s -> s.split(","))
                .stream()
                .flatMap(Arrays::stream)
                .map(prefix::concat);
        this.symbolicNames = Stream.concat(Stream.of(ve.id), tolerated)
                .sequential()
                .collect(toUnmodifiableList());
    }

    @Override
    public boolean matches(Element e) {
        return e instanceof Feature && symbolicNames.contains(e.symbolicName());
    }

    @Override
    public int compareMatches(Element f1, Element f2) {
        return ordinal(f1) - ordinal(f2);
    }

    private int ordinal(Element f) {
        int result = symbolicNames.indexOf(f.symbolicName());
        if (-1 == result)
            throw new IllegalStateException("Cannot establish order for feature " + f + " because it does not occur in the list of acceptable features: " + symbolicNames);
        return result;
    }

    @Override
    public String toString() {
        if (1 == symbolicNames.size()) return symbolicNames.get(0);
        return symbolicNames.get(0).replaceFirst("-[^-]*$", "-") +
                symbolicNames.stream()
                        .map(s -> s.replaceFirst("^[^-]*-", ""))
                        .collect(joining("|", "(", ")"));
    }
}
