# Liberty Explorer - MVP (Minimum Viable Product)

## Overview

This MVP demonstrates the core functionality of the Liberty Explorer web application:
- **Backend**: JAX-RS REST API with mock data
- **Frontend**: React application with feature listing and details
- **Deployment**: Packaged as a WAR file for Liberty server

## What's Included

### Backend Components
- **Data Model**: `Element`, `FeatureInfo`, `BundleInfo`, `ElementType`
- **REST API**: `FeatureResource` with endpoints:
  - `GET /api/features` - List all features
  - `GET /api/features/{symbolicName}` - Get feature details
- **Mock Data**: 4 sample features (servlet, cdi, restfulWS, jsonb)

### Frontend Components
- **React App**: Single-page application with:
  - Feature list view
  - Feature detail panel
  - Responsive design
  - Error handling
- **Styling**: Modern UI with IBM-inspired color scheme

## Quick Start

### Prerequisites
- [SDKMAN](https://sdkman.io/) for managing Java, Gradle, and Node versions
- The project includes `.sdkmanrc` which automatically configures:
  - Java 17.0.11-sem
  - Gradle 8.5
  - Node 20.11.0

### Build and Run

1. **Activate SDKMAN environment** (if not auto-enabled):
   ```bash
   sdk env install
   ```

2. **Install frontend dependencies**:
   ```bash
   cd src/main/webapp/frontend
   npm install
   cd ../../../..
   ```

3. **Build the project**:
   ```bash
   gradle clean build
   ```

4. **Run in development mode**:
   ```bash
   gradle libertyDev
   ```

4. **Access the application**:
   - Welcome page: http://localhost:9080/liberty-explorer/
   - React app: http://localhost:9080/liberty-explorer/static/index.html
   - API: http://localhost:9080/liberty-explorer/api/features

### Development Mode (Frontend Only)

For faster frontend development with hot reload:

```bash
cd src/main/webapp/frontend
npm run dev
```

Access at: http://localhost:3000 (proxies API calls to Liberty server at port 9080)

## Project Structure

```
lx/
├── src/main/
│   ├── java/io/openliberty/tools/explorer/
│   │   ├── model/              # Data models
│   │   │   ├── Element.java
│   │   │   ├── ElementType.java
│   │   │   ├── FeatureInfo.java
│   │   │   └── BundleInfo.java
│   │   └── api/                # REST endpoints
│   │       ├── RestApplication.java
│   │       └── FeatureResource.java
│   ├── webapp/
│   │   ├── frontend/           # React application
│   │   │   ├── src/
│   │   │   │   ├── App.jsx
│   │   │   │   ├── App.css
│   │   │   │   ├── main.jsx
│   │   │   │   └── index.css
│   │   │   ├── index.html
│   │   │   ├── package.json
│   │   │   └── vite.config.js
│   │   ├── WEB-INF/
│   │   │   └── web.xml
│   │   └── index.html          # Welcome page
│   └── liberty/config/
│       └── server.xml
├── build.gradle
└── MVP_README.md
```

## Features Demonstrated

### ✅ Implemented
- [x] REST API with JAX-RS
- [x] JSON serialization with Jakarta JSON-B
- [x] React frontend with Vite
- [x] Feature listing
- [x] Feature details view
- [x] Responsive UI design
- [x] Error handling
- [x] Mock data for demonstration

### 🚧 Not Yet Implemented (Future Phases)
- [ ] Real Liberty installation scanning
- [ ] Bundle exploration
- [ ] Dependency graph visualization
- [ ] Search functionality
- [ ] Auto-feature analysis
- [ ] OSGi component discovery
- [ ] Configuration metadata
- [ ] Runtime state detection

## API Endpoints

### GET /api/features
Returns list of all features.

**Response**:
```json
[
  {
    "symbolicName": "io.openliberty.servlet-6.0",
    "name": "servlet-6.0",
    "version": "1.0.0",
    "description": "Jakarta Servlet 6.0 support",
    "visibility": "PUBLIC",
    "autoFeature": false,
    "dependencies": [],
    "type": "FEATURE"
  }
]
```

### GET /api/features/{symbolicName}
Returns details for a specific feature.

**Response**: Single feature object (same structure as above)

## Testing the MVP

1. **Start the server**:
   ```bash
   ./gradlew libertyDev
   ```

2. **Test the API**:
   ```bash
   curl http://localhost:9080/liberty-explorer/api/features
   ```

3. **Test the UI**:
   - Open http://localhost:9080/liberty-explorer/
   - Click "Launch Explorer"
   - Click on features to view details

4. **Verify features**:
   - Feature list loads successfully
   - Clicking a feature shows details
   - All 4 mock features are displayed
   - Dependencies are shown for restfulWS

## Known Limitations (MVP)

1. **Mock Data Only**: Uses hardcoded sample features, not real Liberty installation
2. **No Persistence**: Data is not stored or cached
3. **Limited API**: Only feature endpoints implemented
4. **No Authentication**: Open access to all endpoints
5. **No Graph Visualization**: Text-based dependency display only
6. **No Search**: Full search functionality not implemented

## Next Steps

To evolve this MVP into a full application:

1. **Phase 2**: Implement real Liberty installation scanning
2. **Phase 3**: Add bundle exploration and package/class browsing
3. **Phase 4**: Integrate graph visualization with Cytoscape.js
4. **Phase 5**: Add search and filtering capabilities
5. **Phase 6**: Implement auto-feature analysis
6. **Phase 7**: Add OSGi component and configuration discovery

## Troubleshooting

### Build Issues
- Ensure Java 17+ is installed: `java -version`
- Ensure Node.js 18+ is installed: `node -version`
- Clean build: `./gradlew clean build`

### Frontend Issues
- Delete node_modules: `rm -rf src/main/webapp/frontend/node_modules`
- Reinstall: `cd src/main/webapp/frontend && npm install`

### Liberty Issues
- Check server logs: `build/wlp/usr/servers/libertyExplorerServer/logs/`
- Verify features in server.xml
- Ensure port 9080 is available

## License

Eclipse Public License 2.0 (EPL-2.0)

## Contributing

This is an MVP demonstration. For production use, additional features and testing are required.