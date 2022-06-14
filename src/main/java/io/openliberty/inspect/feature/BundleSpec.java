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

import io.openliberty.inspect.Bundle;
import io.openliberty.inspect.Element;
import org.osgi.framework.VersionRange;

public class BundleSpec implements ContentSpec {
    final String symbolicName;
    final VersionRange versionRange;

    BundleSpec(ManifestValueEntry ve) {
        this.symbolicName = ve.id;
        this.versionRange = VersionRange.valueOf(ve.getQualifier("version"));
    }

    @Override
    public boolean matches(Element e) {
        return e instanceof Bundle && symbolicName.equals(e.symbolicName()) && versionRange.includes(e.version());
    }

    @Override
    public int compareMatches(Element f1, Element f2) {
        return f1.compareTo(f2);
    }


    @Override
    public String toString() {
        return symbolicName + ":" + versionRange;
    }
}
