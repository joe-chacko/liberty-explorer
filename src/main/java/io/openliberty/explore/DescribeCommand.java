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

import io.openliberty.inspect.Bundle;
import io.openliberty.inspect.Visibility;
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
        explorer().allResults().stream().filter(e -> e.visibility() == Visibility.PUBLIC).forEach(e -> {
                    System.out.println(fg_red.on() + e.name() + ":" + fg_red.off());
                    System.out.println(e.description() + "\n");
                }
        );
        explorer().allResults().stream().filter(e -> e instanceof Bundle).forEach(e -> {
                    System.out.println(fg_red.on() + e.pathName() + ":" + fg_red.off());
                    System.out.println(e.description() + "\n");
                }
        );

//        explorer().allResults().stream().filter(e -> e instanceof Bundle).map(Element::description).forEach(System.out::println);
//        System.out.println("***pathName***");
//        explorer().allResults().stream().filter(e-> e.visibility()== Visibility.PUBLIC).map(Element::pathName).forEach(System.out::println);
//        System.out.println("***Path***");
//        explorer().allResults().stream().filter(e-> e.visibility()== Visibility.PUBLIC).map(Element::path).forEach(System.out::println);
//        System.out.println("***name***");
//        explorer().allResults().stream().filter(e-> e.visibility()== Visibility.PUBLIC).map(Element::name).forEach(System.out::println);
//        System.out.println("***symbolicName***");
//        explorer().allResults().stream().filter(e-> e.visibility()== Visibility.PUBLIC).map(Element::symbolicName).forEach(System.out::println);
//        System.out.println("***fileName***");
//        explorer().allResults().stream().filter(e-> e.visibility()== Visibility.PUBLIC).map(Element::fileName).forEach(System.out::println);
//        System.out.println("***simpleName***");
//        explorer().allResults().stream().filter(e-> e.visibility()== Visibility.PUBLIC).map(Element::simpleName).forEach(System.out::println);
    }
}
