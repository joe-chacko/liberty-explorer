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
package io.openliberty.explore;

import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.concurrent.Callable;

abstract class QueryCommand implements Callable<Integer> {
    @ParentCommand
    private LibertyExplorer explorer;
    @Parameters(arity = "1..*", description = "one or more glob patterns to match features by name")
    private List<String> patterns;

    @Override
    public final Integer call() throws Exception {
        explorer.init(patterns);
        execute();
        return 0;
    }

    abstract void execute() throws Exception;

    LibertyExplorer explorer() { return explorer; }
}
