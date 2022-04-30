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

import java.util.Optional;
import java.util.stream.Stream;

public interface Element extends Comparable<Element> {
    String fullName();
    String name();
    default Optional<String> shortName() { return Optional.empty(); }

    default Visibility visibility() { return Visibility.PRIVATE; }
    default Stream<String> containedElements() { return Stream.empty(); }
    default String simpleName() {
        return name()
                .replaceFirst("^(com.ibm.ws|io.openliberty).(com\\.|org\\.|net\\.)?", "")
                .replaceFirst("^com.ibm.websphere.app(server|client).", "")
                .replaceFirst("^com.ibm.websphere.", "")
                .replaceFirst("^io.openliberty.", "");
    }
}
