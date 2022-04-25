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

import java.util.function.Predicate;
import java.util.jar.Attributes;

@SuppressWarnings("unused")
enum Visibility implements Predicate<Attributes> {
    PUBLIC("@"),
    PROTECTED("~"),
    PRIVATE("-"),
    UNKNOWN("?");

    final String indicator;

    Visibility(String indicator) {
        this.indicator = indicator;
    }

    String format(boolean tabs) {
        return String.format((tabs ? "%s" : "%-10s"), name().toLowerCase());
    }

    public boolean test(Attributes feature) {
        return this == Key.SUBSYSTEM_SYMBOLICNAME.parseValues(feature)
                .findFirst()
                .map(v -> v.getQualifier("visibility"))
                .map(String::toUpperCase)
                .map(Visibility::valueOf)
                .orElse(Visibility.UNKNOWN);
    }
}
