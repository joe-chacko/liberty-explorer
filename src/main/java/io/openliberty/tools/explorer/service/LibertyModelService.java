/*
 * Copyright (c) 2026 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package io.openliberty.tools.explorer.service;

import io.openliberty.tools.explorer.model.FeatureInfo;
import io.openliberty.tools.explorer.model.LibertyModel;
import io.openliberty.tools.explorer.model.LibertyRuntimeScanner;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Application-scoped service that manages the Liberty runtime model.
 * Scans the Liberty installation once at startup and provides access to features and bundles.
 */
@ApplicationScoped
public class LibertyModelService {
    private static final Logger LOGGER = Logger.getLogger(LibertyModelService.class.getName());
    
    private LibertyModel model;
    private boolean initialized = false;
    private String errorMessage;
    
    @PostConstruct
    public void initialize() {
        try {
            Path libertyRoot = detectLibertyRoot();
            Path serverConfigDir = detectServerConfigDir();
            
            LOGGER.info("Scanning Liberty runtime at: " + libertyRoot);
            if (serverConfigDir != null) {
                LOGGER.info("Using server configuration from: " + serverConfigDir);
            }
            
            LibertyRuntimeScanner scanner = new LibertyRuntimeScanner();
            String libertyVersion = detectLibertyVersion();
            String gradlePluginVersion = detectGradlePluginVersion();
            
            model = scanner.scan(libertyRoot, serverConfigDir, libertyVersion, gradlePluginVersion);
            initialized = true;
            
            LOGGER.info("Liberty runtime scan complete. Found " + 
                       model.getFeatures().size() + " features and " + 
                       model.getBundles().size() + " bundles.");
        } catch (Exception e) {
            errorMessage = "Failed to scan Liberty runtime: " + e.getMessage();
            LOGGER.log(Level.SEVERE, errorMessage, e);
            initialized = false;
        }
    }
    
    /**
     * Get the scanned Liberty model.
     * @return the Liberty model, or null if scanning failed
     */
    public LibertyModel getModel() {
        return model;
    }
    
    /**
     * Check if the service was successfully initialized.
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Get the error message if initialization failed.
     * @return error message, or null if no error
     */
    public String getErrorMessage() {
        return errorMessage;
    }
    
    /**
     * Get all features from the scanned Liberty runtime.
     * @return list of features
     */
    public List<FeatureInfo> getFeatures() {
        return model != null ? model.getFeatures() : List.of();
    }
    
    /**
     * Find a feature by its symbolic name or short name.
     * @param name the symbolic name or short name
     * @return the feature if found
     */
    public Optional<FeatureInfo> findFeature(String name) {
        if (model == null) {
            return Optional.empty();
        }
        
        return model.getFeatures().stream()
            .filter(f -> f.getSymbolicName().equals(name) || 
                        f.getName().equals(name) ||
                        f.getSymbolicName().endsWith("." + name))
            .findFirst();
    }
    
    /**
     * Detect the Liberty runtime root directory.
     * Checks multiple locations in order:
     * 1. System property: liberty.runtime.dir
     * 2. Environment variable: WLP_INSTALL_DIR
     * 3. Current server's installation (if running on Liberty)
     * 4. Build directory (for development)
     */
    private Path detectLibertyRoot() {
        // 1. Check system property
        String runtimeDir = System.getProperty("liberty.runtime.dir");
        if (runtimeDir != null) {
            Path path = Paths.get(runtimeDir);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        // 2. Check environment variable
        String wlpInstallDir = System.getenv("WLP_INSTALL_DIR");
        if (wlpInstallDir != null) {
            Path path = Paths.get(wlpInstallDir);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        // 3. Check if running on Liberty (wlp.install.dir system property)
        String wlpInstallDirProp = System.getProperty("wlp.install.dir");
        if (wlpInstallDirProp != null) {
            Path path = Paths.get(wlpInstallDirProp);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        // 4. Check build directory (for development)
        Path buildPath = Paths.get("build/wlp");
        if (Files.isDirectory(buildPath)) {
            return buildPath.toAbsolutePath();
        }
        
        throw new IllegalStateException(
            "Liberty runtime not found. Please set liberty.runtime.dir system property, " +
            "WLP_INSTALL_DIR environment variable, or ensure running on Liberty server."
        );
    }
    
    /**
     * Detect the server configuration directory.
     * Returns null if not found (will scan all features).
     */
    private Path detectServerConfigDir() {
        // Check system property
        String configDir = System.getProperty("liberty.server.config.dir");
        if (configDir != null) {
            Path path = Paths.get(configDir);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        // Check if running on Liberty
        String serverConfigDir = System.getProperty("server.config.dir");
        if (serverConfigDir != null) {
            Path path = Paths.get(serverConfigDir);
            if (Files.isDirectory(path)) {
                return path;
            }
        }
        
        // Check default location in project
        Path defaultPath = Paths.get("src/main/liberty/config");
        if (Files.isDirectory(defaultPath)) {
            return defaultPath.toAbsolutePath();
        }
        
        // Return null to scan all features
        return null;
    }
    
    /**
     * Detect Liberty version from the runtime.
     */
    private String detectLibertyVersion() {
        String version = System.getProperty("liberty.runtime.version");
        if (version != null) {
            return version;
        }
        
        // Try to read from properties file
        try {
            Path libertyRoot = detectLibertyRoot();
            Path propsFile = libertyRoot.resolve("lib/versions/openliberty.properties");
            if (Files.exists(propsFile)) {
                List<String> lines = Files.readAllLines(propsFile);
                for (String line : lines) {
                    if (line.startsWith("com.ibm.websphere.productVersion=")) {
                        return line.substring("com.ibm.websphere.productVersion=".length()).trim();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not detect Liberty version", e);
        }
        
        return "unknown";
    }
    
    /**
     * Detect Gradle plugin version.
     */
    private String detectGradlePluginVersion() {
        String version = System.getProperty("liberty.gradle.plugin.version");
        return version != null ? version : "unknown";
    }
}

// Made with Bob