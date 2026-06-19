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
import java.util.Collections;
import java.util.List;

/**
 * Root model for a scanned Liberty runtime.
 */
public class LibertyModel {
    private final String libertyVersion;
    private final String gradlePluginVersion;
    private final List<FeatureInfo> features;
    private final List<BundleInfo> bundles;

    public LibertyModel(String libertyVersion, String gradlePluginVersion,
            List<FeatureInfo> features, List<BundleInfo> bundles) {
        this.libertyVersion = libertyVersion;
        this.gradlePluginVersion = gradlePluginVersion;
        this.features = Collections.unmodifiableList(new ArrayList<>(features));
        this.bundles = Collections.unmodifiableList(new ArrayList<>(bundles));
    }

    public String getLibertyVersion() {
        return libertyVersion;
    }

    public String getGradlePluginVersion() {
        return gradlePluginVersion;
    }

    public List<FeatureInfo> getFeatures() {
        return features;
    }

    public List<BundleInfo> getBundles() {
        return bundles;
    }
}

// Made with Bob
