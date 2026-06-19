/*
 * Copyright (c) 2026 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package io.openliberty.tools.explorer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Liberty feature for the MVP
 */
public class FeatureInfo implements Element {
    private String symbolicName;
    private String name;
    private String version;
    private String description;
    private String visibility;
    private boolean autoFeature;
    private List<String> dependencies = new ArrayList<>();
    private List<String> dependents = new ArrayList<>();
    private List<List<String>> autoFeatureConditions = new ArrayList<>();

    public FeatureInfo() {}

    public FeatureInfo(String symbolicName, String name, String version) {
        this.symbolicName = symbolicName;
        this.name = name;
        this.version = version;
    }

    @Override
    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public boolean isAutoFeature() {
        return autoFeature;
    }

    public void setAutoFeature(boolean autoFeature) {
        this.autoFeature = autoFeature;
    }

    @Override
    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<String> getDependents() {
        return dependents;
    }

    public void setDependents(List<String> dependents) {
        this.dependents = dependents;
    }

    public List<List<String>> getAutoFeatureConditions() {
        return autoFeatureConditions;
    }

    public void setAutoFeatureConditions(List<List<String>> autoFeatureConditions) {
        this.autoFeatureConditions = autoFeatureConditions;
    }

    @Override
    public ElementType getType() {
        return ElementType.FEATURE;
    }
}

// Made with Bob
