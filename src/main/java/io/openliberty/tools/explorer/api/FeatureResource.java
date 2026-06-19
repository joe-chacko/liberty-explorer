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
import io.openliberty.tools.explorer.service.LibertyModelService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API for Liberty features.
 * Stage 1: Uses real Liberty runtime scanning instead of mock data.
 */
@ApplicationScoped
@Path("/features")
@Produces(MediaType.APPLICATION_JSON)
public class FeatureResource {
    
    @Inject
    private LibertyModelService modelService;
    
    /**
     * Get all features from the scanned Liberty runtime.
     */
    @GET
    public Response getAllFeatures() {
        if (!modelService.isInitialized()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Liberty runtime not initialized");
            error.put("message", modelService.getErrorMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                          .entity(error)
                          .build();
        }
        
        List<FeatureInfo> features = modelService.getFeatures();
        return Response.ok(features).build();
    }
    
    /**
     * Get a specific feature by symbolic name or short name.
     */
    @GET
    @Path("/{name}")
    public Response getFeature(@PathParam("name") String name) {
        if (!modelService.isInitialized()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Liberty runtime not initialized");
            error.put("message", modelService.getErrorMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                          .entity(error)
                          .build();
        }
        
        FeatureInfo feature = modelService.findFeature(name).orElse(null);
        
        if (feature == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Feature not found");
            error.put("name", name);
            return Response.status(Response.Status.NOT_FOUND)
                          .entity(error)
                          .build();
        }
        
        return Response.ok(feature).build();
    }
}

// Made with Bob
