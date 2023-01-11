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

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import io.openliberty.inspect.Bundle;
import io.openliberty.inspect.Element;
import io.openliberty.inspect.feature.Feature;
import org.apache.commons.io.FileUtils;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.dot.DOTExporter;
import picocli.CommandLine.Command;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jgrapht.nio.DefaultAttribute.createAttribute;
import static picocli.CommandLine.Help.Ansi.Style.fg_blue;
import static picocli.CommandLine.Help.Ansi.Style.underline;

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

    String getNodes(String graphDotCode) throws IOException {
        StringBuilder nodes = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new StringReader(graphDotCode));
        String line;
        while ((line = bufReader.readLine()) != null) {
            if (line.contains(" [")) {
                String nodeString = line.split("\\[")[0];
                nodes.append("g.addNode(").append(nodeString).append(");\n");
            }
        }
        return nodes.toString();
    }

    String getEdges(String graphDotCode) throws IOException {
        StringBuilder nodes = new StringBuilder();
        BufferedReader bufReader = new BufferedReader(new StringReader(graphDotCode));
        String line;
        while ((line = bufReader.readLine()) != null) {
            if (line.contains(" ->")) {
                String[] nodeString = line.split("->");
                nodes.append(String.format("g.addEdge(%s,%s, {directed : true});\n", nodeString[0], nodeString[1].replace(";", "")));
            }
        }
        return nodes.toString();
    }

    URI generateInteractiveGraph(String graphDotCode) throws IOException {
        String templateFilePath = "src/main/java/io/openliberty/explore/GraphDisplay/template.html";
        String htmlFilePath = "src/main/java/io/openliberty/explore/GraphDisplay/Graph.html";
        File htmlTemplateFile = new File(templateFilePath);

        String htmlString = FileUtils.readFileToString(htmlTemplateFile, StandardCharsets.UTF_8);
        htmlString = htmlString.replace("$nodes", getNodes(graphDotCode));
        htmlString = htmlString.replace("$edges", getEdges(graphDotCode));

        File newHtmlFile = new File(htmlFilePath);
        FileUtils.writeStringToFile(newHtmlFile, htmlString, StandardCharsets.UTF_8);
        Desktop.getDesktop().browse(newHtmlFile.toURI());
        return newHtmlFile.toURI();
    }

    URI generateSVGGraph(String graphDotCode) throws IOException {
        MutableGraph g = new Parser().read(graphDotCode);
        File svgFile = new File("src/main/java/io/openliberty/explore/GraphDisplay/Graph.svg");
        Graphviz.fromGraph(g).render(Format.SVG).toFile(svgFile);
        Desktop.getDesktop().browse(svgFile.toURI());
        return svgFile.toURI();
    }

    void execute() {
        var exporter = new DOTExporter<Element, DefaultEdge>(this::displayName);
        exporter.setVertexAttributeProvider(this::getDotAttributes);
        var writer = new StringWriter();
        exporter.exportGraph(explorer().subgraph(), writer);
        System.out.println(writer);
        try {
            System.out.println("Interactive Graph: " + underline.on() + fg_blue.on() + generateInteractiveGraph(writer.toString()) + underline.off() + fg_blue.off());
            System.out.println("SVG Graph: " + underline.on() + fg_blue.on() + generateSVGGraph(writer.toString()) + underline.off() + fg_blue.off());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Attribute shape(Element element) {
        if (element instanceof Feature) switch (element.visibility()) {
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
