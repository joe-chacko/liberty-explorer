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

import picocli.CommandLine.Command;

@Command(
        name = "list",
        description = "List matching features"
)
public class ListCommand extends QueryCommand {
    ListCommand() { super(DisplayOption.normal, true);}

    void execute() {
        explorer().allResults().stream().map(this::displayName).sorted().forEach(System.out::println);
    }
}
