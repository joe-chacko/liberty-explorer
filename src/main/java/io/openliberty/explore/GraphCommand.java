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
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTExporter;
import picocli.CommandLine.Command;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.jgrapht.nio.DefaultAttribute.createAttribute;

@Command(
        name = "graph",
        description = "Produce a graph of selected features"
)
public class GraphCommand extends QueryCommand {
    public final Attribute SUBJECT_FILL_COLOR = createAttribute("gray95");

    void execute() {
        var exporter = new DOTExporter<Element, DefaultEdge>(e -> '"' + e.simpleName().replaceAll("\\s","\\\\n") + '"');
        exporter.setVertexAttributeProvider(this::getDotAttributes);
        var writer = new StringWriter();
        exporter.exportGraph(explorer().subgraph(), writer);
        System.out.println(writer);
    }

    private static Attribute shape(Element element) {
        if (element instanceof Feature) switch(element.visibility()) {
            case PUBLIC: return createAttribute("tripleoctagon");
            case PROTECTED: return createAttribute("doubleoctagon");
            case PRIVATE: return createAttribute("octagon");
            case UNKNOWN: return createAttribute("egg");
        }
        if (element instanceof Bundle)
            return createAttribute("cylinder");
        throw new Error("Unknown element type: " + element.getClass());
    }

    private Map<String, Attribute> getDotAttributes(Element element) {
        Map<String, Attribute> result = new HashMap<>();
        result.put("shape", shape(element));
        if (explorer().isPrimary(element)) {
            result.put("bgcolor", SUBJECT_FILL_COLOR);
            result.put("style", createAttribute("filled"));
        }
        return result;
    }
}
