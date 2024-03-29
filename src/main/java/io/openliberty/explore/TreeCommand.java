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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            super(displayName(e));
            this.element = e;
        }
    }

    @SuppressWarnings("unused")
    enum Style {
        ascii(ASCII_ROUNDED),
        unicode(UNICODE_ROUNDED),
        windows(WIN_TREE);
        final TreeStyle treeStyle;
        Style(TreeStyle treeStyle) { this.treeStyle = treeStyle; }
    }

    @Option(names = "--style", description = "Choose a tree style from the following: ${COMPLETION-CANDIDATES}")
    Style style = Style.unicode;

    TreeCommand() {super(DisplayOption.simple, true);}

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
        String render = TEXT_TREE.render(root);
        Stream.of(render.split(System.lineSeparator()))
                .map(this::moveScopeToStartOfString)
                .forEach(System.out::println);
    }
}
