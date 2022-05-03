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


import io.openliberty.inspect.Catalog;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand;
import picocli.CommandLine.Option;
import picocli.CommandLine.PropertiesDefaultProvider;

import java.util.concurrent.Callable;

@Command(
        name = "lx",
        description = "Liberty installation eXplorer",
        subcommands = {GraphCommand.class, HelpCommand.class},
        defaultValueProvider = PropertiesDefaultProvider.class
)
public class LibertyExplorer implements Callable<Integer> {

    @Option(names = { "-d", "--directory"},
            description = "Liberty root directory (defaults to the working directory)",
            defaultValue = "."
    )
    Catalog liberty;

    @Option(names = {"-v", "--verbose"})
    boolean verbose;


    public static void main(String[] args) {
        LibertyExplorer command = new LibertyExplorer();
        CommandLine commandLine = new CommandLine(command);
        commandLine.registerConverter(Catalog.class, Catalog::new);
        int exitCode = commandLine.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        System.out.println("Hello, world.");
        return 0;
    }
}
