/*
 * Copyright (c) 2026 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package io.openliberty.tools.explorer.api;

import io.openliberty.tools.explorer.model.FeatureInfo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * REST API for Liberty features (MVP with mock data)
 */
@ApplicationScoped
@Path("/features")
@Produces(MediaType.APPLICATION_JSON)
public class FeatureResource {
    
    /**
     * Get all features
     */
    @GET
    public Response getAllFeatures() {
        List<FeatureInfo> features = getMockFeatures();
        return Response.ok(features).build();
    }
    
    /**
     * Get a specific feature by symbolic name
     */
    @GET
    @Path("/{symbolicName}")
    public Response getFeature(@PathParam("symbolicName") String symbolicName) {
        List<FeatureInfo> features = getMockFeatures();
        FeatureInfo feature = features.stream()
            .filter(f -> f.getSymbolicName().equals(symbolicName))
            .findFirst()
            .orElse(null);
        
        if (feature == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(feature).build();
    }
    
    /**
     * Mock data for MVP demonstration
     */
    private List<FeatureInfo> getMockFeatures() {
        List<FeatureInfo> features = new ArrayList<>();
        
        FeatureInfo servlet = new FeatureInfo(
            "io.openliberty.servlet-6.0",
            "servlet-6.0",
            "1.0.0"
        );
        servlet.setDescription("Jakarta Servlet 6.0 support");
        servlet.setVisibility("PUBLIC");
        servlet.setAutoFeature(false);
        features.add(servlet);
        
        FeatureInfo cdi = new FeatureInfo(
            "io.openliberty.cdi-4.0",
            "cdi-4.0",
            "1.0.0"
        );
        cdi.setDescription("Jakarta Contexts and Dependency Injection 4.0");
        cdi.setVisibility("PUBLIC");
        cdi.setAutoFeature(false);
        features.add(cdi);
        
        FeatureInfo restfulWS = new FeatureInfo(
            "io.openliberty.restfulWS-3.1",
            "restfulWS-3.1",
            "1.0.0"
        );
        restfulWS.setDescription("Jakarta RESTful Web Services 3.1");
        restfulWS.setVisibility("PUBLIC");
        restfulWS.setAutoFeature(false);
        restfulWS.setDependencies(Arrays.asList("io.openliberty.servlet-6.0"));
        features.add(restfulWS);
        
        FeatureInfo jsonb = new FeatureInfo(
            "io.openliberty.jsonb-3.0",
            "jsonb-3.0",
            "1.0.0"
        );
        jsonb.setDescription("Jakarta JSON Binding 3.0");
        jsonb.setVisibility("PUBLIC");
        jsonb.setAutoFeature(false);
        features.add(jsonb);
        
        return features;
    }
}

// Made with Bob
