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
import org.barfuin.texttree.api.Node;
import org.barfuin.texttree.api.TextTree;
import org.barfuin.texttree.api.TreeOptions;
import org.barfuin.texttree.api.color.DefaultColorScheme;
import picocli.CommandLine.Command;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static org.barfuin.texttree.api.CycleProtection.PruneRepeating;
import static org.barfuin.texttree.api.style.TreeStyles.UNICODE_ROUNDED;

@Command(
        name = "tree",
        description = "Produce an ascii tree of selected features"
)
public class TreeCommand extends QueryCommand {
    private enum TreeUtil {
        ;
        static final TextTree TEXT_TREE;
        static {
            var opts = new TreeOptions();
            opts.setCycleProtection(PruneRepeating);
            opts.setStyle(UNICODE_ROUNDED);
            opts.setColorScheme(new DefaultColorScheme());
            TEXT_TREE = TextTree.newInstance(opts);
        }
        static String render(Node n) { return TEXT_TREE.render(n); }
    }

    static class TreeNode extends DefaultNode {
        final Element element;

        TreeNode(Element e) {
            super(e.toString());
            this.element = e;
        }
    }

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
        System.out.println(TreeUtil.render(root));
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
