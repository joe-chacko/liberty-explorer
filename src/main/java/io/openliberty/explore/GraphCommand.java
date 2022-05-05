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

import io.openliberty.inspect.Element;
import io.openliberty.inspect.Visibility;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTExporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import javax.management.Query;
import java.io.StringWriter;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.openliberty.inspect.Visibility.PRIVATE;
import static io.openliberty.inspect.Visibility.PROTECTED;
import static io.openliberty.inspect.Visibility.PUBLIC;
import static io.openliberty.inspect.Visibility.UNKNOWN;
import static java.util.Collections.unmodifiableMap;
import static org.jgrapht.nio.DefaultAttribute.createAttribute;

@Command(
        name = "graph",
        description = "Produce a graph of selected features"
)
public class GraphCommand implements Runnable {
    public final Attribute SUBJECT_FILL_COLOR = createAttribute("gray95");

    @ParentCommand
    LibertyExplorer explorer;

    @Parameters(arity = "1..*", description = "one or more glob patterns to match features by name")
    List<String> patterns;

    @SuppressWarnings("ThrowablePrintedToSystemOut")
    public void run() {
        try {
            explorer.init(patterns);

            var exporter = new DOTExporter<Element, DefaultEdge>(f -> '"' + f.simpleName() + '"');
            exporter.setVertexAttributeProvider(this::getDotAttributes);
            var writer = new StringWriter();
            exporter.exportGraph(explorer.subgraph(), writer);
            System.out.println(writer.toString());
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println(t);
        }
    }


    private static final Map<Visibility, Attribute> SHAPE_FOR_VISIBILITY = unmodifiableMap(new EnumMap<>(Map.of(
            PUBLIC, createAttribute("tripleoctagon"),
            PROTECTED, createAttribute("doubleoctagon"),
            PRIVATE, createAttribute("octagon"),
            UNKNOWN, createAttribute("egg")
    )));

    private Map<String, Attribute> getDotAttributes(Element feature) {
        Map<String, Attribute> result = new HashMap<>();
        result.put("shape", SHAPE_FOR_VISIBILITY.get(feature.visibility()));
        if (explorer.isPrimary(feature)) {
            result.put("bgcolor", SUBJECT_FILL_COLOR);
            result.put("style", createAttribute("filled"));
        }
        return result;
    }

}
