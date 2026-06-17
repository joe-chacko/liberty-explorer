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

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
    // Empty class - JAX-RS will auto-discover resources
}

// Made with Bob
