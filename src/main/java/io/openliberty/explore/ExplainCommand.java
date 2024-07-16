/*
 * =============================================================================
 * Copyright (c) 2022,2024 IBM Corporation and others.
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

import java.util.EnumMap;
import java.util.List;
import static java.util.function.Predicate.not;
import java.util.stream.Collectors;

import io.openliberty.inspect.Visibility;
import io.openliberty.inspect.feature.Feature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

@Command(
        name = "explain",
        description = "Produce a detailed description of the specified elements"
)
public class ExplainCommand {
    private static final String BULLET_POINT = "  - ";
    
    @ParentCommand
    private LibertyExplorer explorer;

    @Command(name = "features", description = "Explain a feature")
    void features(
            @Parameters(arity = "1..*", description = "one or more glob patterns to match features by name", defaultValue = "*")
            List<String> patterns
            ) throws Exception {
        explorer.init(patterns); // find all features
        explorer.allResults()
                .stream()
                .filter(Feature.class::isInstance)
                .map(Feature.class::cast)
                .sorted()
                .forEach(this::explain);
    }

    private static EnumMap<Visibility, String> VIS_DESCS = new EnumMap<>(Visibility.class);

    private void explain(Feature f) {
        // Heading
        String name = f.name();
        System.out.println(name);
        System.out.println(name.replaceAll(".", "="));
        // Feature names
        System.out.printf("The feature %s is defined by the file: %s%n", f.symbolicName(), f.path());
        f.shortName().ifPresent(n -> System.out.printf("It is also known by its short name: %s%n", n));
        // Describe visibility, auto-ness, and include embedded description
        System.out.printf(switch (f.visibility()) {
            case PUBLIC -> "This is a public feature; it can be configured directly in server configuration,"
                    + " or included by Liberty or extension features.%n"
                    + f.description() + "%n";
            case PROTECTED -> "This is a protected feature; it can be included by Liberty or extension features.%n";
            case PRIVATE -> f.isAutoFeature() ?
                    "This is an auto-feature; it will be included automatically based on the presence of other features in combination.%n" :
                    "This is a private feature; it can be included by other Liberty features.%n";
            default -> "The visibility of this feature is unknown.%n";
        });
        boolean hasFeatureDeps = f.hasFeatureDependencies();
        if (hasFeatureDeps) {
            System.out.println("This feature includes the following features:");
            f.formatFeatureDependencies().map(BULLET_POINT::concat).forEach(System.out::println);
        }
        if (f.hasBundleDependencies()) {
            System.out.println(hasFeatureDeps ? "and the following bundles:" : "This feature includes the following bundles:");
            f.formatBundleDependencies().map(BULLET_POINT::concat).forEach(System.out::println);
        }
        System.out.println();
    }
}
