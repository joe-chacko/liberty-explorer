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

import static org.jgrapht.nio.DefaultAttribute.createAttribute;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTExporter;

import io.openliberty.inspect.Bundle;
import io.openliberty.inspect.Element;
import io.openliberty.inspect.feature.Feature;
import picocli.CommandLine.Command;

@Command(
        name = "graph",
        description = "Produce a graph of selected features"
)
public class GraphCommand extends QueryCommand {
    public static final Attribute SUBJECT_FILL_COLOR = createAttribute("gray95");

    GraphCommand() { super(DisplayOption.simple, false); }

    @Override
    String displayName(Element e) {
        return '"'
                + super.displayName(e).replaceAll("\\s", "\\\\n")
                + '"';
    }

    void execute() {
        var exporter = new DOTExporter<Element, DefaultEdge>(this::displayName);
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
        List<String> styles = new ArrayList<>();
        Map<String, Attribute> result = new HashMap<>();
        result.put("shape", shape(element));
        if (explorer().isPrimary(element)) {
            result.put("bgcolor", SUBJECT_FILL_COLOR);
            styles.add("filled");
            styles.add("bold");
        }
        if (element.isAutoFeature()) {
            styles.add("dashed");
        }
        result.put("style", createAttribute(String.join(",", styles)));
        return result;
    }
}
