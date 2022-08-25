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
import io.openliberty.inspect.Element;
import io.openliberty.inspect.feature.Feature;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

abstract class QueryCommand implements Callable<Integer> {
    @ParentCommand
    private LibertyExplorer explorer;
    @Parameters(arity = "1..*", description = "one or more glob patterns to match features by name")
    private List<String> patterns;

    QueryCommand(DisplayOption defaultDisplay) {
        display = defaultDisplay;
    }

    @Override
    public final Integer call() throws Exception {
        explorer.init(patterns);
        execute();
        return 0;
    }

    abstract void execute() throws Exception;

    LibertyExplorer explorer() { return explorer; }

    @SuppressWarnings("unused")
    enum DisplayOption {
        normal(Element::toString),
        simple(Element::simpleName),
        symbolic(Element::symbolicName),
        path(Element::pathName),
        file(Element::fileName),
        full(Element::name)
        ;
        final Function<Element, String> fun;
        DisplayOption(Function<Element, String> fun) { this.fun = fun; }
        String getName(Element e) { return fun.apply(e); }
    }

    @Option(names = {"--display", "-d"}, description = "Control how elements are displayed: ${COMPLETION-CANDIDATES}")
    private DisplayOption display = DisplayOption.normal;

    @Option(names = {"-s", "--scope"}, description = "Display additional scope information:" +
            "\n\t +++ - public features" +
            "\n\t === - protected features" +
            "\n\t --- - private features" +
            "\n\t -A- - auto-features" +
            "\n\t -b- - bundles" +
            "\n\t ??? - unknown")
    private boolean scope = true;

    private String prefix(Element e) {
        if (!scope) return "";
        if (e instanceof Bundle) return "-b- ";
        if (e.isAutoFeature()) return "-a- ";
        switch(e.visibility()) {
            case PUBLIC: return "+++ ";
            case PROTECTED: return "=== ";
            case PRIVATE: return "--- ";
            default: return "??? ";
        }
    }

    String displayName(Element e) {
        return prefix(e) + display.getName(e);
    }
}
