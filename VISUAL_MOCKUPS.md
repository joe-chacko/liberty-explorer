# Liberty Explorer - Visual Mockups

This document provides detailed visual mockups for each implementation phase of the Liberty Explorer interface.

---

## Phase 1: Features Only - Initial State

### Mockup 1.1: Welcome Screen (Before Search)

```mermaid
graph TB
    subgraph Window["Liberty Explorer - Browser Window"]
        subgraph Header["Header Bar - Dark Gray #161616"]
            Logo["IBM Liberty Explorer"]
            About["ℹ️ About"]
        end
        
        subgraph MainContent["Main Content Area - Light Gray #f4f4f4"]
            subgraph Sidebar["Sidebar - 30% - White #ffffff"]
                SearchBox["🔍 Search features, bundles, classes..."]
                FilterSection["Filter by:<br/>☑ Features 150"]
                EmptyState["No search results<br/>Start typing to search"]
            end
            
            subgraph MainArea["Main Area - 70% - White #ffffff"]
                Welcome["<h1>Welcome to Liberty Explorer</h1><br/>Search for features, bundles, and more to get started.<br/><br/>Try searching for:<br/>• servlet<br/>• cdi<br/>• rest*"]
            end
        end
    end
    
    Header --> MainContent
    Sidebar -.-> MainArea
```

---

## Phase 1: Features Only - Search Results

### Mockup 1.2: Search Results with Compact View

```mermaid
graph TB
    subgraph Window["Liberty Explorer - Browser Window"]
        subgraph Header["Header Bar"]
            Logo["IBM Liberty Explorer"]
            About["ℹ️"]
        end
        
        subgraph MainContent["Main Content Area"]
            subgraph Sidebar["Sidebar - 30%"]
                SearchBox["🔍 servlet"]
                FilterSection["Filter by:<br/>☑ Features 3"]
                ResultsHeader["Results: 3 items"]
                
                subgraph Results["Search Results - Compact"]
                    R1["🎯 servlet-6.0"]
                    R2["🎯 servlet-5.0"]
                    R3["🎯 servlet-4.0"]
                end
            end
            
            subgraph MainArea["Main Area - 70%"]
                Breadcrumb["Home > Search Results"]
                ViewToggle["[Tree View] [Graph View]"]
                EmptySelection["Select an item from the search results<br/>to view details"]
            end
        end
    end
    
    Header --> MainContent
    SearchBox --> FilterSection
    FilterSection --> ResultsHeader
    ResultsHeader --> Results
    Breadcrumb --> ViewToggle
    ViewToggle --> EmptySelection
    
    style R1 fill:#e0e0e0
```

### Mockup 1.3: Search Results with Medium Density

```mermaid
graph TB
    subgraph Sidebar["Sidebar - Search Results Medium Density"]
        SearchBox["🔍 servlet"]
        FilterSection["Filter by:<br/>☑ Features 3"]
        ResultsHeader["Results: 3 items<br/>[Density: Compact | Medium | Detailed]"]
        
        subgraph Results["Search Results"]
            subgraph R1["Result Item 1 - Selected"]
                R1Icon["🎯"]
                R1Name["servlet-6.0"]
                R1Meta["v1.0.0 | PUBLIC | 3 bundles"]
            end
            
            subgraph R2["Result Item 2"]
                R2Icon["🎯"]
                R2Name["servlet-5.0"]
                R2Meta["v1.0.0 | PUBLIC | 3 bundles"]
            end
            
            subgraph R3["Result Item 3"]
                R3Icon["🎯"]
                R3Name["servlet-4.0"]
                R3Meta["v1.0.0 | PUBLIC | 2 bundles"]
            end
        end
    end
    
    SearchBox --> FilterSection
    FilterSection --> ResultsHeader
    ResultsHeader --> Results
    
    style R1 fill:#0f62fe,color:#fff
    style R2 fill:#fff
    style R3 fill:#fff
```

---

## Phase 1: Tree View

### Mockup 1.4: Tree View - Feature Details

```mermaid
graph TB
    subgraph Window["Liberty Explorer - Tree View"]
        subgraph Sidebar["Sidebar"]
            SearchBox["🔍 servlet"]
            Results["Results: 3<br/><br/>🎯 servlet-6.0 ◀ Selected<br/>🎯 servlet-5.0<br/>🎯 servlet-4.0"]
        end
        
        subgraph MainArea["Main Area"]
            Breadcrumb["Home > servlet-6.0"]
            ViewToggle["[Tree View] Graph View"]
            
            subgraph TreeView["Tree View Content"]
                subgraph FeatureNode["🎯 servlet-6.0 Feature"]
                    subgraph Details["📝 Details Expanded"]
                        D1["Symbolic Name: io.openliberty.servlet-6.0"]
                        D2["Version: 1.0.0"]
                        D3["Visibility: PUBLIC"]
                        D4["Description: Jakarta Servlet 6.0 support"]
                    end
                    
                    subgraph Dependencies["🔗 Dependencies 2 Expanded"]
                        Dep1["→ javaee-8.0 clickable link"]
                        Dep2["→ servlet.api-3.1 clickable link"]
                    end
                    
                    subgraph Actions["📊 Actions"]
                        Action1["[Show in Graph]"]
                        Action2["[Copy Details]"]
                    end
                end
            end
        end
    end
    
    Sidebar -.-> MainArea
    Breadcrumb --> ViewToggle
    ViewToggle --> TreeView
    
    style FeatureNode fill:#e8f4fd
    style Details fill:#fff
    style Dependencies fill:#fff
    style Actions fill:#fff
```

### Mockup 1.5: Tree View - Hover Tooltip

```mermaid
graph TB
    subgraph TreeView["Tree View with Hover"]
        subgraph FeatureNode["🎯 servlet-6.0"]
            Dependencies["🔗 Dependencies 2"]
            Dep1["→ javaee-8.0"]
        end
        
        subgraph Tooltip["Tooltip on Hover"]
            TooltipContent["javaee-8.0 Feature<br/>Version: 8.0.0<br/>Visibility: PUBLIC<br/><br/>Click to navigate"]
        end
    end
    
    Dep1 -.->|hover| Tooltip
    
    style Tooltip fill:#161616,color:#fff
```

---

## Phase 1: Graph View

### Mockup 1.6: Graph View - Basic Feature Dependencies

```mermaid
graph TB
    subgraph Window["Liberty Explorer - Graph View"]
        subgraph Sidebar["Sidebar"]
            SearchBox["🔍 servlet"]
            Results["Results: 3<br/><br/>🎯 servlet-6.0 ◀<br/>🎯 servlet-5.0<br/>🎯 servlet-4.0"]
        end
        
        subgraph MainArea["Main Area"]
            Breadcrumb["Home > servlet-6.0"]
            ViewToggle["Tree View [Graph View]"]
            
            subgraph Controls["Graph Controls"]
                Layout["Layout: Hierarchical ▼"]
                Zoom["Zoom: 100% ▼"]
                Export["Export ▼"]
                Reset["Reset"]
            end
            
            subgraph GraphCanvas["Graph Canvas"]
                subgraph Graph["Dependency Graph"]
                    Servlet["servlet-6.0<br/>v1.0.0"]
                    JavaEE["javaee-8.0<br/>v8.0.0"]
                    ServletAPI["servlet.api-3.1<br/>v3.1.0"]
                    
                    Servlet -->|depends on| JavaEE
                    Servlet -->|depends on| ServletAPI
                end
            end
        end
    end
    
    Sidebar -.-> MainArea
    Controls --> GraphCanvas
    
    style Servlet fill:#0f62fe,color:#fff,stroke:#000,stroke-width:3px
    style JavaEE fill:#0f62fe,color:#fff,stroke:#000,stroke-width:2px
    style ServletAPI fill:#0f62fe,color:#fff,stroke:#000,stroke-width:2px
```

### Mockup 1.7: Graph View - With Filter Panel

```mermaid
graph TB
    subgraph GraphArea["Graph View with Filters"]
        subgraph Controls["Graph Controls Bar"]
            Layout["Layout: Hierarchical ▼"]
            Zoom["Zoom: 100% ▼"]
            Export["Export ▼"]
            Reset["Reset"]
            FilterToggle["[Filters ▼]"]
        end
        
        subgraph FilterPanel["Filter Panel Expanded"]
            subgraph NodeFilters["Node Types"]
                F1["☑ Show Features"]
                F2["☐ Show Bundles"]
                F3["☐ Show Classes"]
            end
            
            subgraph RelFilters["Relationships"]
                R1["☑ Show Dependencies"]
                R2["☐ Show Containment"]
            end
            
            subgraph DepthControl["Depth"]
                D1["● 1  ○ 2  ○ 3  ○ All"]
            end
        end
        
        subgraph GraphCanvas["Graph Canvas"]
            Graph["Feature nodes with<br/>dependency edges"]
        end
    end
    
    Controls --> FilterPanel
    FilterPanel --> GraphCanvas
    
    style FilterPanel fill:#f4f4f4
```

### Mockup 1.8: Graph View - Node Context Menu

```mermaid
graph TB
    subgraph GraphView["Graph View - Right-Click Context Menu"]
        subgraph Graph["Graph Canvas"]
            Node["servlet-6.0<br/>v1.0.0<br/><right-click>"]
        end
        
        subgraph ContextMenu["Context Menu"]
            M1["View Details"]
            M2["Expand Dependencies"]
            M3["Show Parent Features"]
            M4["Focus on this node"]
            M5["Hide this node"]
            M6["View in Tree"]
            M7["Copy Symbolic Name"]
        end
    end
    
    Node -.->|right-click| ContextMenu
    
    style Node fill:#0f62fe,color:#fff,stroke:#000,stroke-width:3px
    style ContextMenu fill:#fff,stroke:#000,stroke-width:1px
```

---

## Phase 2: Features + Bundles

### Mockup 2.1: Search Results - Mixed Types

```mermaid
graph TB
    subgraph Sidebar["Sidebar - Mixed Search Results"]
        SearchBox["🔍 servlet"]
        
        subgraph Filters["Filter by:"]
            F1["☑ Features 3"]
            F2["☑ Bundles 5"]
            F3["☐ Classes 0"]
        end
        
        ResultsHeader["Results: 8 items"]
        
        subgraph Results["Search Results - Grouped by Type"]
            subgraph FeatureGroup["Features 3"]
                FR1["🎯 servlet-6.0"]
                FR2["🎯 servlet-5.0"]
                FR3["🎯 servlet-4.0"]
            end
            
            subgraph BundleGroup["Bundles 5"]
                BR1["📦 com.ibm.ws.servlet.3.1"]
                BR2["📦 com.ibm.ws.servlet.security"]
                BR3["📦 com.ibm.ws.servlet.api"]
                BR4["📦 ..."]
            end
        end
    end
    
    SearchBox --> Filters
    Filters --> ResultsHeader
    ResultsHeader --> Results
    
    style FeatureGroup fill:#e8f4fd
    style BundleGroup fill:#e8f9f0
```

### Mockup 2.2: Tree View - Feature with Bundles

```mermaid
graph TB
    subgraph TreeView["Tree View - Feature with Bundles Expanded"]
        subgraph FeatureNode["🎯 servlet-6.0 Feature"]
            Details["📝 Details<br/>Symbolic Name: io.openliberty.servlet-6.0<br/>Version: 1.0.0<br/>Visibility: PUBLIC"]
            
            subgraph Bundles["📦 Bundles 3 Expanded"]
                B1["com.ibm.ws.servlet.3.1<br/>v3.1.0 | 45 classes"]
                B2["com.ibm.ws.servlet.security<br/>v3.1.0 | 12 classes"]
                B3["com.ibm.ws.servlet.api<br/>v3.1.0 | 8 classes"]
            end
            
            Dependencies["🔗 Dependencies 2<br/>→ javaee-8.0<br/>→ servlet.api-3.1"]
            
            Actions["📊 Actions<br/>[Show in Graph] [Copy Details]"]
        end
    end
    
    Details --> Bundles
    Bundles --> Dependencies
    Dependencies --> Actions
    
    style FeatureNode fill:#e8f4fd
    style Bundles fill:#e8f9f0
```

### Mockup 2.3: Tree View - Bundle Details with Traceability

```mermaid
graph TB
    subgraph TreeView["Tree View - Bundle Details"]
        Breadcrumb["Home > servlet-6.0 > com.ibm.ws.servlet.3.1"]
        
        subgraph BundleNode["📦 com.ibm.ws.servlet.3.1 Bundle"]
            Details["📝 Details<br/>Symbolic Name: com.ibm.ws.servlet.3.1<br/>Version: 3.1.0<br/>State: Active"]
            
            subgraph ParentFeatures["← Contained in Features 2"]
                PF1["→ servlet-6.0 clickable"]
                PF2["→ javaee-8.0 clickable"]
            end
            
            ExportedPkg["📄 Exported Packages 5<br/>Collapsed"]
            
            ImportedPkg["📄 Imported Packages 12<br/>Collapsed"]
            
            Actions["📊 Actions<br/>[Show in Graph] [Show Parent Features in Graph]"]
        end
    end
    
    Breadcrumb --> BundleNode
    Details --> ParentFeatures
    ParentFeatures --> ExportedPkg
    ExportedPkg --> ImportedPkg
    ImportedPkg --> Actions
    
    style BundleNode fill:#e8f9f0
    style ParentFeatures fill:#e8f4fd
```

### Mockup 2.4: Graph View - Features and Bundles

```mermaid
graph TB
    subgraph GraphView["Graph View - Features and Bundles"]
        subgraph Controls["Controls"]
            FilterPanel["Filters:<br/>☑ Features ☑ Bundles<br/>☑ Dependencies ☑ Containment"]
        end
        
        subgraph Graph["Graph Canvas"]
            subgraph FeatureLayer["Feature Layer"]
                F1["servlet-6.0"]
                F2["javaee-8.0"]
            end
            
            subgraph BundleLayer["Bundle Layer"]
                B1["com.ibm.ws.servlet.3.1"]
                B2["com.ibm.ws.servlet.security"]
                B3["com.ibm.ws.servlet.api"]
            end
            
            F1 -->|depends on| F2
            F1 -.->|contains| B1
            F1 -.->|contains| B2
            F1 -.->|contains| B3
        end
    end
    
    Controls --> Graph
    
    style F1 fill:#0f62fe,color:#fff,stroke:#000,stroke-width:3px
    style F2 fill:#0f62fe,color:#fff,stroke:#000,stroke-width:2px
    style B1 fill:#24a148,color:#fff,stroke:#000,stroke-width:2px
    style B2 fill:#24a148,color:#fff,stroke:#000,stroke-width:2px
    style B3 fill:#24a148,color:#fff,stroke:#000,stroke-width:2px
```

---

## Phase 3: Classes and Packages

### Mockup 3.1: Search Results - All Types

```mermaid
graph TB
    subgraph Sidebar["Sidebar - All Types Search"]
        SearchBox["🔍 servlet"]
        
        subgraph Filters["Filter by:"]
            F1["☑ Features 3"]
            F2["☑ Bundles 5"]
            F3["☑ Classes 15"]
            F4["☐ DS Components 0"]
            F5["☐ Config 0"]
        end
        
        ResultsHeader["Results: 23 items<br/>[Show: 20 per page]"]
        
        subgraph Results["Search Results"]
            Features["Features 3<br/>🎯 servlet-6.0<br/>🎯 servlet-5.0<br/>🎯 servlet-4.0"]
            
            Bundles["Bundles 5<br/>📦 com.ibm.ws.servlet.3.1<br/>📦 com.ibm.ws.servlet.security<br/>..."]
            
            Classes["Classes 15<br/>📄 ServletContext<br/>📄 HttpServlet<br/>📄 ServletRequest<br/>..."]
        end
    end
    
    SearchBox --> Filters
    Filters --> ResultsHeader
    ResultsHeader --> Results
```

### Mockup 3.2: Tree View - Class Details with Full Traceability

```mermaid
graph TB
    subgraph TreeView["Tree View - Class Details"]
        Breadcrumb["Home > ServletContext"]
        
        subgraph ClassNode["📄 ServletContext Class"]
            Details["📝 Details<br/>Fully Qualified Name: jakarta.servlet.ServletContext<br/>Package: jakarta.servlet<br/>Type: Interface<br/>Modifiers: public"]
            
            subgraph ParentBundle["← Contained in Bundle"]
                PB["→ com.ibm.ws.servlet.api clickable"]
            end
            
            subgraph ParentFeatures["← Contained in Features via Bundle"]
                PF1["→ servlet-6.0 clickable"]
                PF2["→ javaee-8.0 clickable"]
            end
            
            Methods["Methods 25<br/>Collapsed"]
            
            Actions["📊 Actions<br/>[Show in Graph] [Show Full Path in Graph]"]
        end
    end
    
    Breadcrumb --> ClassNode
    Details --> ParentBundle
    ParentBundle --> ParentFeatures
    ParentFeatures --> Methods
    Methods --> Actions
    
    style ClassNode fill:#fff4e6
    style ParentBundle fill:#e8f9f0
    style ParentFeatures fill:#e8f4fd
```

---

## Responsive Behavior

### Mockup R.1: Mobile/Tablet Layout

```mermaid
graph TB
    subgraph Mobile["Mobile View - Stacked Layout"]
        Header["IBM Liberty Explorer"]
        
        SearchBar["🔍 Search"]
        
        FilterToggle["[Filters ▼]"]
        
        Results["Results: 3<br/>🎯 servlet-6.0<br/>🎯 servlet-5.0<br/>🎯 servlet-4.0"]
        
        ViewToggle["[Tree] [Graph]"]
        
        Content["Selected Item Details<br/>Full Width"]
    end
    
    Header --> SearchBar
    SearchBar --> FilterToggle
    FilterToggle --> Results
    Results --> ViewToggle
    ViewToggle --> Content
```

---

## Dark Mode Support

### Mockup D.1: Dark Mode Color Scheme

```mermaid
graph TB
    subgraph DarkMode["Dark Mode - Color Palette"]
        Background["Background: #161616"]
        Surface["Surface: #262626"]
        Primary["Primary: #0f62fe"]
        Text["Text: #f4f4f4"]
        
        subgraph NodeColors["Node Colors - Dark Mode"]
            F["Features: #4589ff"]
            B["Bundles: #42be65"]
            C["Classes: #ff832b"]
            D["DS: #be95ff"]
            E["Config: #3ddbd9"]
        end
    end
    
    style Background fill:#161616,color:#f4f4f4
    style Surface fill:#262626,color:#f4f4f4
    style Primary fill:#0f62fe,color:#fff
    style Text fill:#f4f4f4,color:#161616
    style F fill:#4589ff,color:#fff
    style B fill:#42be65,color:#000
    style C fill:#ff832b,color:#000
    style D fill:#be95ff,color:#000
    style E fill:#3ddbd9,color:#000
```

---

## Loading States

### Mockup L.1: Loading States

```mermaid
graph TB
    subgraph LoadingStates["Loading States"]
        subgraph InitialLoad["Initial Load"]
            Skeleton["Skeleton Screens<br/>Gray placeholder boxes<br/>for sidebar and main area"]
        end
        
        subgraph SearchLoading["Search Loading"]
            SearchSpinner["Search box with spinner<br/>Results area shows loading"]
        end
        
        subgraph GraphLoading["Graph Loading"]
            GraphSpinner["Graph canvas with<br/>Loading... message<br/>and spinner"]
        end
        
        subgraph TreeLoading["Tree Expansion Loading"]
            TreeSpinner["Expanding node shows<br/>inline spinner"]
        end
    end
```

---

## Error States

### Mockup E.1: Error Handling

```mermaid
graph TB
    subgraph ErrorStates["Error States"]
        subgraph SearchError["Search Error"]
            ErrorMsg["⚠️ Unable to load search results<br/>Please try again<br/>[Retry Button]"]
        end
        
        subgraph NoResults["No Results"]
            NoResultsMsg["No results found for 'xyz'<br/>Try different search terms"]
        end
        
        subgraph GraphError["Graph Error"]
            GraphErrorMsg["⚠️ Unable to load graph<br/>The graph may be too large<br/>[Try with filters] [Retry]"]
        end
        
        subgraph APIError["API Error"]
            APIErrorMsg["⚠️ Connection error<br/>Unable to reach server<br/>[Retry]"]
        end
    end
```

---

## Summary

These mockups illustrate:

1. **Progressive Enhancement**: From simple feature list to complex multi-type exploration
2. **Consistent Layout**: Sidebar + Main area pattern throughout
3. **Clear Visual Hierarchy**: Icons, colors, and spacing guide the eye
4. **Traceability**: Breadcrumbs and clickable links show relationships
5. **Flexible Views**: Tree for hierarchy, Graph for relationships
6. **Responsive Design**: Adapts to different screen sizes
7. **Accessibility**: Clear labels, icons, and color-independent indicators
8. **Error Handling**: Graceful degradation with helpful messages

All mockups follow Carbon Design System principles and maintain consistency across phases.