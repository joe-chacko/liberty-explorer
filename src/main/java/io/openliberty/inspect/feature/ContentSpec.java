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

import java.util.Collection;
import java.util.Optional;

public interface ContentSpec {
    default Optional<Element> findBestMatch(Collection<Element> elements) {
        return elements.stream()
                .filter(this::matches)
                .max(this::compareMatches);
    }

    boolean matches(Element e);

    int compareMatches(Element f1, Element f2);
}
