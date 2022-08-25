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
import org.barfuin.texttree.api.DefaultNode;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.color.DefaultColorScheme;
import org.barfuin.texttree.api.style.TreeStyle;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.barfuin.texttree.api.CycleProtection.PruneRepeating;
import static org.barfuin.texttree.api.style.TreeStyles.ASCII_ROUNDED;
import static org.barfuin.texttree.api.style.TreeStyles.UNICODE_ROUNDED;
import static org.barfuin.texttree.api.style.TreeStyles.WIN_TREE;

@Command(
        name = "tree",
        description = "Produce an ascii tree of selected features"
)
public class TreeCommand extends QueryCommand {
    class TreeNode extends DefaultNode {
        final Element element;

        TreeNode(Element e) {
            super(display.getName(e));
            this.element = e;
        }
    }

    @SuppressWarnings("unused")
    enum Style {
        ASCII(ASCII_ROUNDED),
        UNICODE(UNICODE_ROUNDED),
        WINDOWS(WIN_TREE);
        final TreeStyle treeStyle;
        Style(TreeStyle treeStyle) { this.treeStyle = treeStyle; }
    }

    @SuppressWarnings("unused")
    enum DisplayName {
        TO_STRING(Element::toString),
        SIMPLE(Element::simpleName),
        SYMBOLIC(Element::symbolicName),
        FULL_NAME(Element::name)
        ;
        final Function<Element, String> fun;
        DisplayName(Function<Element, String> fun) { this.fun = fun; }
        String getName(Element e) { return fun.apply(e); }
    }

    @Option(names = {"--style", "-s"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    Style style = Style.UNICODE;

    @Option(names = {"--display", "-d"}, description = "Valid values: ${COMPLETION-CANDIDATES}")
    DisplayName display = DisplayName.TO_STRING;

    void execute() {
        var graph = explorer().subgraph();
        var nodeMap = new HashMap<Element, TreeNode>();
        // create one node for each element
        graph.vertexSet().stream()
                .map(TreeNode::new)
                .forEach(n -> nodeMap.put(n.element, n));
        nodeMap.values().stream()
                .forEach(parentElement -> {
                    graph.outgoingEdgesOf(parentElement.element).stream()
                            .map(graph::getEdgeTarget)
                            .map(nodeMap::get)
                            .forEach(parentElement::addChild);
                });
        Set<TreeNode> roots = graph.vertexSet().stream()
                .filter(v -> graph.inDegreeOf(v) == 0)
                .map(nodeMap::get)
                .collect(Collectors.toSet());
        if (roots.isEmpty()) return;
        final DefaultNode root;
        if (roots.size() == 1) {
            root = roots.iterator().next();
        } else {
            root = new DefaultNode("Multiple root nodes found");
            roots.forEach(root::addChild);
        }

        var opts = new TreeOptions();
        opts.setCycleProtection(PruneRepeating);
        opts.setStyle(style.treeStyle);
        opts.setColorScheme(new DefaultColorScheme());
        final TextTree TEXT_TREE = TextTree.newInstance(opts);


        System.out.println(TEXT_TREE.render(root));
    }


    private void xxx(Element element) {
        if (element instanceof Feature) switch(element.visibility()) {
            case PUBLIC:
            case PROTECTED:
            case PRIVATE:
            case UNKNOWN:
        }
        else if (element instanceof Bundle) {}
        else throw new Error("Unknown element type: " + element.getClass());
        if (explorer().isPrimary(element)) {
        }
        if (element.isAutoFeature()) {
        }
    }
}
