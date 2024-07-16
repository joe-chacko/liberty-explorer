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

package io.openliberty.inspect;

import io.openliberty.inspect.feature.Feature;
import org.apache.commons.collections4.Bag;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.bag.HashBag;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.jgrapht.Graph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.Files.isDirectory;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;

public class Catalog {
    public static SimpleDirectedGraph<Element, DefaultEdge> newGraph() {
        return new SimpleDirectedGraph<>(DefaultEdge.class);
    }

    private final Map<String, Element> elements = new HashMap<>();
    // Wrap (downcased) feature names and shortnames as Path objects
    // to allow use of java.nio.file.FileSystem's built-in glob matching
    private final MultiValuedMap<Path, Element> index = new HashSetValuedHashMap<>();
    private final SimpleDirectedGraph<Element, DefaultEdge> dependencies = newGraph();

    public Catalog(Path libertyRoot, boolean includeBundles) throws IOException {
        validate(libertyRoot, "Not a valid directory: ");
        Path libDir = validate(libertyRoot.resolve("lib"), "No lib subdirectory found: ");
        // parse bundles
        if (includeBundles) Files.list(libDir)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".jar"))
                .map(Bundle::new)
                .forEach(this::initElement);
        Path featureDir = validate(libertyRoot.resolve("lib/features"), "No feature subdirectory found: ");
        Path platformDir = validate(libertyRoot.resolve("lib/platform"), "No platform subdirectory found: ");
        // parse feature manifests
        Stream.concat(Files.list(platformDir), Files.list(featureDir))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".mf"))
                .map(Feature::new)
                .forEach(this::initElement);
        // add the features to the graph
        elements.values().forEach(dependencies::addVertex);
        // add the feature dependencies to the graph
        elements.values().forEach(this::initDependencies);
    }

    private void initElement(Element e) {
        // add to element map
        elements.put(e.symbolicName(), e);
        // add to graph
        dependencies.addVertex(e);
        // add to index using full name and short name (if present)
        e.allNames()
                .map(String::toLowerCase)
                .map(Paths::get)
                .forEach(k -> index.put(k, e));
    }

    public static void main(String[] args) throws Exception {
        NameUtil.prefixes("io.openliberty.jakarta.3.0_1.0.63.jar")
                .forEach(System.out::println);
        System.out.println();
        System.out.println();
        System.out.println();
        Path p = Paths.get("/Users/chackoj/wlp/lib");
        var names = Files.list(p)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(s -> s.endsWith(".jar"))
                .collect(Collectors.toUnmodifiableSet());
        NameUtil.shortenNames(names)
                .entrySet()
                .stream()
                .sorted(comparing(Entry::getValue))
                .forEach(e -> System.out.println(e.getValue()));
    }


    enum NameUtil {
        ;
        private static Pattern PREFIX_DELIMITER = Pattern.compile("(?<!\\d)\\.|(?<=\\d\\.\\d)\\.|_|\\.(?!\\d)");
        private static Stream<String> prefixes(String name) {
            var m = PREFIX_DELIMITER.matcher(name);
            var prefixes = new ArrayList<String>();
            if (m.find()) {
                prefixes.add(name.substring(0, m.start()));
                while (m.find(m.end())) {
                    prefixes.add(name.substring(0, m.start()));
                }
            }
            prefixes.add(name);
            return prefixes.stream();
        }

        private static Map<String, String> shortenNames(Set<String> names) {
            var trie = new PatriciaTrie<String>();
            names.stream().forEach(name -> {
                // remove some common prefixes
                var shorterName = name
                        .replaceFirst("^com\\.ibm\\.ws\\.", "ws ")
                        .replaceFirst("^io\\.openliberty\\.", "ol ")
                        .replaceFirst("^(ws|ol) (com|org|net)\\.([^.]+)\\.", "$1 $3 ")
                        .replaceFirst("^com\\.ibm\\.websphere\\.(app(server|client)\\.)?", "websphere $1 ");
                // add the shortened name to a trie
                var oldName = trie.put(shorterName, name);
                if (null != oldName) {
                    // this is a bug that must be fixed in this code - bail out
                    throw new Error("Initial shortening of " + oldName + " and " + name + " both resulted in " + shorterName);
                }
            });
            var map = new TreeMap<String, String>();
            trie.forEach((k, v) -> map.put(v, minPrefix(k, trie)));

            // fudge 1: try chomping substrings and check for conflicts
            var pattern = "apache\\.";
            var strings = map.values();
            Bag<String> bag = strings.stream()
                    .map(s -> s.replaceFirst(pattern, ""))
                    .collect(HashBag::new, HashBag::add, HashBag::addAll);
            map.entrySet().stream()
//                    .filter(e -> e.getValue())
                    .forEach(e -> {});


            return Collections.unmodifiableMap(map);
        }

        private static String minPrefix(String key, Trie<String, ?> trie) {
            var smallestSize = trie.prefixMap(key).size();
            Predicate<String> isMinimal = s -> trie.prefixMap(s).size() == smallestSize;
            try {
                return prefixes(key)
                        .filter(isMinimal)
                        .findFirst()
                        .orElseThrow(Exception::new);
            } catch (Exception ex) {
                System.err.println("Failed to find min prefix for " + key);
                System.err.println(trie.prefixMap(key));
                ex.printStackTrace();
                return key;
            }
        }
    }


    private void initDependencies(Element e) {
        e.findDependencies(elements.values())
                .forEach(d -> dependencies.addEdge(e, d));
    }

    private static Path validate(Path path, String errorMessage) {
        if (isDirectory(path)) return path;
        throw new Error(errorMessage + path.toFile().getAbsolutePath());
    }

    public Stream<Element> findMatches(String pattern) {
        pattern = requireNonNull(pattern).toLowerCase();
        if (!pattern.contains(":")) pattern = "glob:" + pattern;
        return index.keySet().stream()
                .filter(FileSystems.getDefault().getPathMatcher(pattern)::matches)
                .map(index::get)
                .flatMap(Collection::stream)
                .filter(dependencies::containsVertex)
                .sorted()
                .distinct();
    }

    public Graph<Element, DefaultEdge> dependencyGraph() { return new AsUnmodifiableGraph<>(dependencies); }

    public void exclude(Element excluded) { dependencies.removeVertex(excluded); }
}
