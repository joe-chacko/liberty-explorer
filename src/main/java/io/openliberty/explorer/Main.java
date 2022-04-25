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
package io.openliberty.explorer;

import io.openliberty.explorer.feature.Catalog;
import io.openliberty.explorer.feature.Feature;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        System.out.println("================================================================================");
        try {
            Catalog liberty = new Catalog(Paths.get("/Users/chackoj/wlp"));
            liberty.doSomething();
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println(t);
        }
    }
}
