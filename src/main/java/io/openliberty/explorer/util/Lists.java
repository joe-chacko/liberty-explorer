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
package io.openliberty.explorer.util;

import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toUnmodifiableList;
import static java.util.stream.Stream.concat;

enum Lists {
    ;

    @SafeVarargs
    static <T> List<T> append(List<T> list, T... items) {
        return concat(list.stream(), stream(items)).collect(toUnmodifiableList());
    }

    static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    static <T> Comparator<List<T>> comparingEachElement(Comparator<T> elementOrder) {
        return (l1, l2) -> {
            for (int i = 0; i < Math.min(l1.size(), l2.size()); i++) {
                int c = elementOrder.compare(l1.get(i), l2.get(i));
                if (c != 0) return c;
            }
            return l1.size() - l2.size();
        };
    }
}
