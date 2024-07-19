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

import static java.util.Collections.unmodifiableMap;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ManifestValueEntry {
    private static final String TEXT = "([^\";\\\\]|\\\\.)+";
    private static final String QUOTED_TEXT = "\"([^\\\\\"]|\\\\.)+\"";
    private static final Pattern ATOM_PATTERN = Pattern.compile(String.format("(%s|%s)+", TEXT, QUOTED_TEXT));
    final String id;
    private final Map<? extends String, String> qualifiers;

    ManifestValueEntry(String text) {
        Matcher m = ATOM_PATTERN.matcher(text);
        if (!m.find()) throw new Error("Unable to parse manifest value into constituent parts: " + text);
        this.id = m.group();
        Map<String, String> map = new TreeMap<>();
        while (m.find(m.end())) {
            String[] parts = m.group().split(":?=", 2);
            if (parts.length == 1) {
                System.out.println(m.group());
                System.out.flush();
                System.exit(1);
            }
            String oldValue = map.put(parts[0].trim(), parts[1].trim().replaceFirst("^\"(.*)\"$", "$1"));
            if (null != oldValue)
                System.err.printf("WARNING: duplicate metadata key '%s' detected in string '%s'", parts[0], text);
        }
        this.qualifiers = unmodifiableMap(map);
    }

    String getQualifier(String key) {
        return qualifiers.get(key);
    }

    String getQualifierOrDefault(String key, String defaultValue) {
        return qualifiers.getOrDefault(key, defaultValue);
    }

    Optional<String> getQualifierIfPresent(String key) {
        return Optional.ofNullable(qualifiers.get(key));
    }

    public String toString() {
        return String.format("%88s : %s", id, qualifiers);
    }
}
