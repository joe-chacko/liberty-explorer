<!--
  Copyright (c) 2026 IBM Corporation.
  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License 2.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-2.0/
  
  SPDX-License-Identifier: EPL-2.0
-->

# Liberty Explorer Web UI

A web-based UI for exploring IBM Open Liberty installations. Provides interactive visualization and deep exploration of features, bundles, OSGi Declarative Services, configuration metadata, and their relationships.

## Features

- **Interactive Dependency Graphs** - Visualize feature and bundle dependencies with Cytoscape.js
- **Auto-Feature Analysis** - Show trigger conditions and relationships
- **DS Component Explorer** - Browse OSGi Declarative Services components
- **Configuration Discovery** - Explore metatype configuration metadata
- **Package/Class Browser** - Search and browse Java packages and classes within bundles
- **Runtime State Detection** - Distinguish loaded vs installed features/bundles
- **Multi-faceted Search** - Search across features, bundles, packages, classes, components, and config

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed system architecture, data models, and API specifications.

## Prerequisites

- Java 17 or later
- Node.js 18+ and npm (for frontend development)
- Gradle 8+

## Project Structure

```
liberty-explorer-web/
├── src/
│   ├── main/
│   │   ├── java/              # Backend Java code
│   │   ├── resources/         # Backend resources
│   │   └── webapp/
│   │       ├── WEB-INF/       # Web application config
│   │       └── frontend/      # React frontend
│   └── test/
│       ├── java/              # Test code
│       └── resources/         # Test resources
├── build.gradle               # Gradle build configuration
├── settings.gradle            # Gradle settings
└── README.md                  # This file
```

## Quick Start

### Prerequisites Setup (using SDKMAN)

```bash
# Install SDKMAN if not already installed
curl -s "https://get.sdkman.io" | bash

# Install required versions
sdk install java 17.0.13-tem
sdk install gradle 8.5
sdk install node 20.18.1

# Use the installed versions
sdk use java 17.0.13-tem
sdk use gradle 8.5
sdk use node 20.18.1
```

### Build and Run

```bash
# Start the Liberty server (builds everything automatically)
gradle libertyStart

# Access the application
# - Welcome page: http://localhost:9080/liberty-explorer/
# - React frontend: http://localhost:9080/liberty-explorer/static/
# - API endpoint: http://localhost:9080/liberty-explorer/api/features

# Stop the server
gradle libertyStop
```

## Development

### Backend Development

```bash
# Build the project
gradle build

# Run tests
gradle test

# Clean build
gradle clean build
```

### Frontend Development

The React frontend is located in `src/main/webapp/frontend/`:

```bash
cd src/main/webapp/frontend

# Install dependencies (done automatically by Gradle)
npm install

# Build for production (done automatically by Gradle)
npm run build

# Development mode with hot reload
npm run dev
```

The frontend build is automatically integrated into the Gradle build process.

## Building

```bash
# Full build (compiles Java, builds React, packages WAR)
gradle build

# The output WAR file will be in build/libs/liberty-explorer.war
```

## Deployment

### Using Gradle (Development)

```bash
# Start Liberty server with auto-deployment
gradle libertyStart

# The server will:
# 1. Install Liberty if needed
# 2. Build the frontend
# 3. Package the WAR
# 4. Deploy to dropins directory
# 5. Start the server
```

### Manual Deployment (Production)

1. Build the WAR file: `gradle clean build`
2. Copy `build/libs/liberty-explorer.war` to your Liberty server's `dropins/` directory
3. Liberty will auto-deploy the application

### Liberty Server Configuration

Minimal `server.xml` for dropins deployment:

```xml
<server description="Liberty Explorer Web UI Server">
    <featureManager>
        <feature>restfulWS-3.1</feature>
        <feature>jsonb-3.0</feature>
        <feature>cdi-4.0</feature>
        <feature>servlet-6.0</feature>
    </featureManager>
    
    <logging traceSpecification="*=info" 
             maxFileSize="20" 
             maxFiles="10"/>
</server>
```

## MVP Implementation Status

✅ **MVP Complete** - Basic functionality working:

- [x] Project structure and Gradle build
- [x] Architecture documentation
- [x] Data model implementation (Element, FeatureInfo, BundleInfo)
- [x] REST API layer (GET /api/features, GET /api/features/{name})
- [x] Frontend React application with feature list and detail view
- [x] Welcome page with navigation
- [x] Automated build and deployment

### Current Features

- **Backend**: JAX-RS REST API with mock data for 4 Liberty features
- **Frontend**: React 18 with Vite, displays feature list and details
- **Deployment**: Automated Gradle build with Liberty integration

### Next Steps

- [ ] Connect to real Liberty installation data
- [ ] Graph visualization with Cytoscape.js
- [ ] Search functionality
- [ ] Bundle explorer
- [ ] DS component viewer
- [ ] Configuration metadata browser

## License

This project is licensed under the Eclipse Public License 2.0 (EPL-2.0).

## Contributing

This project follows IBM's contribution guidelines. All contributions must include proper copyright headers and SPDX license identifiers.