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
package io.openliberty;

import org.jgrapht.Graph;
import org.jgrapht.graph.AsGraphUnion;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public enum GraphCollectors {
    ;
    public static <V,E> Collector<Graph<V, E>, ?, Graph<V, E>> toUnionWith(Graph<V,E> initialGraph) { return new ToUnion<>(initialGraph); }

    private static class ToUnion<V,E> implements Collector<Graph<V,E>,ToUnion,Graph<V,E>> {
        Graph<V, E> graph;
        ToUnion(Graph<V,E> initialGraph) { this.graph = initialGraph; }
        void add(Graph<V, E> graphToAdd) { graph = new AsGraphUnion<>(graph, graphToAdd); }
        ToUnion addAll(ToUnion that) { add(that.graph); return this; }
        public Supplier<ToUnion> supplier() {return () -> this;}
        public BiConsumer<ToUnion, Graph<V, E>> accumulator() {return ToUnion::add;}
        public BinaryOperator<ToUnion> combiner() {return ToUnion::addAll;}
        public Function<ToUnion, Graph<V, E>> finisher() {return h -> h.graph;}
        public Set<Characteristics> characteristics() {return Collections.emptySet();}
    }
}
