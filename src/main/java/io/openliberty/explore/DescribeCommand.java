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
import static picocli.CommandLine.Help.Ansi.Style.fg_red;

@Command(
        name = "describe",
        description = "Display description for matching features"
)
public class DescribeCommand extends QueryCommand {

    DescribeCommand() {
        super(DisplayOption.normal, true);
    }

    void execute() {
        explorer().allResults().stream().map(e -> "" + fg_red.on() + this.displayName(e) + ":" + fg_red.off() + "\n" + e.description() + "\n").
                sorted().
                forEach(System.out::println);
    }
}
