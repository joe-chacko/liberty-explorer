# Liberty Explorer - Interface Design Specification

## Design Philosophy

**Primary Goal**: Create a search-first, exploration-focused interface that helps users discover and understand Liberty features, bundles, and their complex relationships through progressive disclosure.

**Key Principles**:
- **Search-First**: Unified search is the primary interaction pattern
- **Progressive Disclosure**: Start simple (features only), expand complexity as needed
- **Traceability**: Easy navigation up and down the hierarchy (features → bundles → classes → DS → config)
- **Accessibility**: Colorblind-friendly design with icons, patterns, and text labels
- **Phased Implementation**: Build incrementally (Phase 1: Features, Phase 2: Bundles, etc.)

---

## Overall Layout

### Master Layout Structure

```mermaid
graph TB
    subgraph App["Liberty Explorer Application"]
        Header["Header: IBM Liberty Explorer | About"]
        
        subgraph MainLayout["Main Layout - Horizontal Split"]
            subgraph Sidebar["Sidebar - 30% Width"]
                Search["Search Box with Type-ahead"]
                Filters["Type Filters"]
                Results["Search Results List"]
            end
            
            subgraph MainArea["Main Area - 70% Width"]
                Breadcrumb["Breadcrumb Navigation"]
                ViewToggle["Tree View / Graph View Toggle"]
                Content["Content Area - Tree or Graph"]
            end
        end
    end
    
    Header --> MainLayout
    Search --> Filters
    Filters --> Results
    Breadcrumb --> ViewToggle
    ViewToggle --> Content
```

---

## Component Specifications

### 1. Header Component

**Carbon Components**: `Header`, `HeaderName`, `HeaderGlobalBar`, `HeaderGlobalAction`

```mermaid
graph LR
    Header["Header"]
    HeaderName["IBM Liberty Explorer"]
    GlobalBar["Global Bar"]
    AboutBtn["About Button"]
    
    Header --> HeaderName
    Header --> GlobalBar
    GlobalBar --> AboutBtn
```

**Implementation**:
```jsx
<Header aria-label="IBM Liberty Explorer">
  <HeaderName href="#" prefix="IBM">
    Liberty Explorer
  </HeaderName>
  <HeaderGlobalBar>
    <HeaderGlobalAction aria-label="About" tooltipAlignment="end">
      <Information size={20} />
    </HeaderGlobalAction>
  </HeaderGlobalBar>
</Header>
```

---

### 2. Sidebar Components

#### 2.1 Search Box with Type-ahead

```mermaid
graph TD
    SearchBox["Search Input"]
    UserTypes["User Types Query"]
    Debounce["Debounce 300ms"]
    APICall["API: GET /api/search?q=query"]
    Suggestions["Suggestions Dropdown"]
    SelectItem["User Selects Item"]
    UpdateResults["Update Results List"]
    
    SearchBox --> UserTypes
    UserTypes --> Debounce
    Debounce --> APICall
    APICall --> Suggestions
    Suggestions --> SelectItem
    SelectItem --> UpdateResults
```

**Features**:
- Unified search across all types
- Fuzzy text matching
- Wildcard pattern support (glob, regex)
- Real-time suggestions as user types
- Debounced search (300ms)

**Carbon Component**: `Search`

#### 2.2 Type Filters

```mermaid
graph TD
    FilterPanel["Filter Panel"]
    
    subgraph Phase1["Phase 1: Features Only"]
        F1["☑ Features 150"]
    end
    
    subgraph Phase2["Phase 2+: All Types"]
        F2["☑ Features 150"]
        B2["☑ Bundles 500"]
        C2["☐ Classes 10,000"]
        D2["☐ DS Components 250"]
        E2["☐ Config Elements 180"]
        
        subgraph Additional["Additional Filters"]
            PubOnly["☑ Public Only"]
            AutoOnly["☐ Auto-Features Only"]
        end
    end
    
    FilterPanel --> Phase1
    FilterPanel --> Phase2
```

**Carbon Component**: `Checkbox` group or `FilterableMultiSelect`

#### 2.3 Search Results List

**Result Item Structure**:

```mermaid
graph LR
    ResultItem["Result Item"]
    Icon["Type Icon"]
    Name["Item Name"]
    Type["Type Label"]
    Details["Quick Details"]
    
    ResultItem --> Icon
    ResultItem --> Name
    ResultItem --> Type
    ResultItem --> Details
```

**Icons by Type** (Colorblind-Friendly):
- 🎯 Features (Target icon)
- 📦 Bundles (Package icon)
- 📄 Classes (Document icon)
- ⚙️ DS Components (Settings icon)
- 📝 Config Elements (Edit icon)

**Density Levels**:

```mermaid
graph TD
    Compact["Compact: Icon + Name + Type"]
    Medium["Medium: + Version + Visibility + Count"]
    Detailed["Detailed: + Symbolic Name + Description"]
    
    DensityToggle["Density Toggle"] --> Compact
    DensityToggle --> Medium
    DensityToggle --> Detailed
```

---

### 3. Main Area Components

#### 3.1 Breadcrumb Navigation

```mermaid
graph LR
    Home["Home"] --> Feature["servlet-6.0"]
    Feature --> Bundle["com.ibm.ws.servlet.3.1"]
    Bundle --> Class["ServletContext"]
    
    style Class fill:#e0e0e0
```

**Carbon Component**: `Breadcrumb`, `BreadcrumbItem`

**Purpose**: Show navigation path, allow quick navigation back

#### 3.2 View Toggle

```mermaid
graph LR
    ViewToggle["View Toggle"]
    TreeView["Tree View"]
    GraphView["Graph View"]
    
    ViewToggle --> TreeView
    ViewToggle --> GraphView
```

**Carbon Component**: `ContentSwitcher` with `Switch` components

---

### 4. Tree View Structure

#### Phase 1: Features Only

```mermaid
graph TD
    Feature["🎯 servlet-6.0 Feature"]
    Details["📝 Details"]
    SymbolicName["Symbolic Name: io.openliberty.servlet-6.0"]
    Version["Version: 1.0.0"]
    Visibility["Visibility: PUBLIC"]
    Description["Description: Jakarta Servlet 6.0 support"]
    
    Dependencies["🔗 Dependencies 2"]
    Dep1["→ javaee-8.0"]
    Dep2["→ servlet.api-3.1"]
    
    Actions["📊 Actions"]
    ShowGraph["Show in Graph"]
    CopyDetails["Copy Details"]
    
    Feature --> Details
    Details --> SymbolicName
    Details --> Version
    Details --> Visibility
    Details --> Description
    
    Feature --> Dependencies
    Dependencies --> Dep1
    Dependencies --> Dep2
    
    Feature --> Actions
    Actions --> ShowGraph
    Actions --> CopyDetails
```

#### Phase 2: Features + Bundles

```mermaid
graph TD
    Feature["🎯 servlet-6.0 Feature"]
    Details["📝 Details"]
    
    Bundles["📦 Bundles 3"]
    Bundle1["com.ibm.ws.servlet.3.1"]
    Bundle2["com.ibm.ws.servlet.security"]
    Bundle3["com.ibm.ws.servlet.api"]
    
    Dependencies["🔗 Dependencies 2"]
    Dep1["→ javaee-8.0"]
    Dep2["→ servlet.api-3.1"]
    
    Actions["📊 Actions"]
    
    Feature --> Details
    Feature --> Bundles
    Bundles --> Bundle1
    Bundles --> Bundle2
    Bundles --> Bundle3
    
    Feature --> Dependencies
    Dependencies --> Dep1
    Dependencies --> Dep2
    
    Feature --> Actions
```

#### Phase 3+: Full Hierarchy

```mermaid
graph TD
    Feature["🎯 servlet-6.0 Feature"]
    Details["📝 Details"]
    
    Bundles["📦 Bundles 3"]
    Bundle1["+ com.ibm.ws.servlet.3.1"]
    
    subgraph BundleExpanded["Bundle Expanded"]
        ExportedPkg["📄 Exported Packages 5"]
        ImportedPkg["📄 Imported Packages 12"]
        DSComp["⚙️ DS Components 3"]
        ConfigElem["📝 Config Elements 1"]
    end
    
    Dependencies["🔗 Dependencies 2"]
    DSComponents["⚙️ DS Components 5"]
    ConfigElements["📝 Config Elements 2"]
    Actions["📊 Actions"]
    
    Feature --> Details
    Feature --> Bundles
    Bundles --> Bundle1
    Bundle1 --> BundleExpanded
    
    Feature --> Dependencies
    Feature --> DSComponents
    Feature --> ConfigElements
    Feature --> Actions
```

#### Tree Node Interactions

```mermaid
graph TD
    Node["Tree Node"]
    
    Click["Click"]
    DoubleClick["Double Click"]
    RightClick["Right Click"]
    Hover["Hover"]
    
    ClickAction["Select Node"]
    DoubleClickAction["Expand/Collapse"]
    ContextMenu["Show Context Menu"]
    Tooltip["Show Tooltip"]
    
    Node --> Click
    Node --> DoubleClick
    Node --> RightClick
    Node --> Hover
    
    Click --> ClickAction
    DoubleClick --> DoubleClickAction
    RightClick --> ContextMenu
    Hover --> Tooltip
    
    subgraph ContextMenuItems["Context Menu Options"]
        ViewDetails["View Details"]
        ShowInGraph["Show in Graph"]
        ShowParents["Show Parent Features"]
        CopyName["Copy Symbolic Name"]
        Export["Export Details"]
    end
    
    ContextMenu --> ContextMenuItems
```

---

### 5. Graph View

#### 5.1 Graph Controls Panel

```mermaid
graph TD
    ControlPanel["Graph Controls Panel"]
    
    subgraph LayoutControls["Layout Controls"]
        LayoutSelect["Layout Selector"]
        Hierarchical["Hierarchical"]
        ForceDirected["Force-Directed"]
        Circular["Circular"]
        Grid["Grid"]
        
        LayoutSelect --> Hierarchical
        LayoutSelect --> ForceDirected
        LayoutSelect --> Circular
        LayoutSelect --> Grid
    end
    
    subgraph ViewControls["View Controls"]
        ZoomIn["Zoom In"]
        ZoomOut["Zoom Out"]
        FitScreen["Fit to Screen"]
        Reset["Reset View"]
    end
    
    subgraph ExportControls["Export Controls"]
        ExportPNG["Export PNG"]
        ExportSVG["Export SVG"]
        ExportJSON["Export JSON"]
    end
    
    subgraph FilterControls["Filter Controls"]
        NodeTypes["Node Types Filter"]
        Relationships["Relationships Filter"]
        Depth["Depth Limiter"]
    end
    
    ControlPanel --> LayoutControls
    ControlPanel --> ViewControls
    ControlPanel --> ExportControls
    ControlPanel --> FilterControls
```

#### 5.2 Node and Edge Design

**Node Shapes** (Colorblind-Friendly):

```mermaid
graph LR
    subgraph NodeTypes["Node Types by Shape"]
        Feature["▭ Feature<br/>Rounded Rectangle"]
        Bundle["⬡ Bundle<br/>Hexagon"]
        Class["● Class<br/>Circle"]
        DS["◆ DS Component<br/>Diamond"]
        Config["⬠ Config<br/>Pentagon"]
    end
    
    style Feature fill:#0f62fe,stroke:#000,stroke-width:2px
    style Bundle fill:#24a148,stroke:#000,stroke-width:2px
    style Class fill:#ff832b,stroke:#000,stroke-width:2px
    style DS fill:#8a3ffc,stroke:#000,stroke-width:2px
    style Config fill:#009d9a,stroke:#000,stroke-width:2px
```

**Edge Types**:

```mermaid
graph LR
    A["Node A"] -->|Dependencies<br/>Solid Arrow| B["Node B"]
    C["Node C"] -.->|Contains<br/>Dashed Arrow| D["Node D"]
    E["Node E"] -.->|Implements<br/>Dotted Arrow| F["Node F"]
    G["Node G"] ==>|Exports<br/>Double Line| H["Node H"]
    I["Node I"] -.->|Imports<br/>Thin Arrow| J["Node J"]
```

#### 5.3 Graph Interaction Model

```mermaid
graph TD
    GraphNode["Graph Node"]
    
    subgraph Interactions["User Interactions"]
        Click["Click"]
        DoubleClick["Double Click"]
        RightClick["Right Click"]
        Hover["Hover"]
        Drag["Drag"]
    end
    
    subgraph Actions["Actions"]
        Select["Select Node"]
        ShowDetails["Show Details in Sidebar"]
        Expand["Expand Relationships"]
        ContextMenu["Show Context Menu"]
        Tooltip["Show Tooltip"]
        Move["Move Node Position"]
    end
    
    GraphNode --> Click
    GraphNode --> DoubleClick
    GraphNode --> RightClick
    GraphNode --> Hover
    GraphNode --> Drag
    
    Click --> Select
    Select --> ShowDetails
    DoubleClick --> Expand
    RightClick --> ContextMenu
    Hover --> Tooltip
    Drag --> Move
```

**Context Menu Options**:

```mermaid
graph TD
    ContextMenu["Right-Click Context Menu"]
    
    ExpandBundles["Expand Bundles"]
    ExpandDeps["Expand Dependencies"]
    ExpandDS["Expand DS Components"]
    ShowParents["Show Parent Features"]
    Focus["Focus on this node"]
    Hide["Hide this node"]
    ViewTree["View in Tree"]
    
    ContextMenu --> ExpandBundles
    ContextMenu --> ExpandDeps
    ContextMenu --> ExpandDS
    ContextMenu --> ShowParents
    ContextMenu --> Focus
    ContextMenu --> Hide
    ContextMenu --> ViewTree
```

#### 5.4 Graph Filter Panel

```mermaid
graph TD
    FilterPanel["Graph Filter Panel"]
    
    subgraph NodeTypeFilters["Node Type Filters"]
        ShowFeatures["☑ Show Features"]
        ShowBundles["☑ Show Bundles"]
        ShowClasses["☐ Show Classes"]
        ShowDS["☑ Show DS Components"]
        ShowConfig["☑ Show Config Elements"]
    end
    
    subgraph RelationshipFilters["Relationship Filters"]
        ShowDependencies["☑ Show Dependencies"]
        ShowContainment["☑ Show Containment"]
        ShowImplements["☐ Show Implements"]
        ShowExports["☐ Show Exports"]
        ShowImports["☐ Show Imports"]
    end
    
    subgraph DepthControl["Depth Control"]
        Depth1["Depth: 1"]
        Depth2["Depth: 2"]
        Depth3["Depth: 3"]
        DepthAll["Depth: All"]
    end
    
    subgraph VisibilityFilters["Visibility Filters"]
        PublicOnly["☑ Public Features Only"]
        ProtectedOnly["☐ Protected Features Only"]
    end
    
    FilterPanel --> NodeTypeFilters
    FilterPanel --> RelationshipFilters
    FilterPanel --> DepthControl
    FilterPanel --> VisibilityFilters
```

---

## Phased Implementation Roadmap

```mermaid
gantt
    title Liberty Explorer Implementation Phases
    dateFormat YYYY-MM-DD
    section Phase 1
    Features Only MVP           :p1, 2026-07-01, 3w
    Search & Tree View          :p1a, 2026-07-01, 2w
    Basic Graph View            :p1b, 2026-07-15, 1w
    
    section Phase 2
    Add Bundles                 :p2, 2026-07-22, 3w
    Bundle Search               :p2a, 2026-07-22, 1w
    Feature-Bundle Tree         :p2b, 2026-07-29, 1w
    Bundle Graph Nodes          :p2c, 2026-08-05, 1w
    
    section Phase 3
    Add Classes & Packages      :p3, 2026-08-12, 4w
    Class Search                :p3a, 2026-08-12, 1w
    Package Browser             :p3b, 2026-08-19, 1w
    Class Graph Nodes           :p3c, 2026-08-26, 2w
    
    section Phase 4
    Add DS Components           :p4, 2026-09-09, 3w
    DS Search                   :p4a, 2026-09-09, 1w
    DS Details & XML            :p4b, 2026-09-16, 1w
    DS Graph Nodes              :p4c, 2026-09-23, 1w
    
    section Phase 5
    Add Configuration           :p5, 2026-09-30, 3w
    Config Search               :p5a, 2026-09-30, 1w
    Metatype Viewer             :p5b, 2026-10-07, 1w
    Config Graph Nodes          :p5c, 2026-10-14, 1w
    
    section Phase 6
    Advanced Features           :p6, 2026-10-21, 4w
    Advanced Filtering          :p6a, 2026-10-21, 1w
    Comparison Views            :p6b, 2026-10-28, 1w
    Export & Saved Searches     :p6c, 2026-11-04, 1w
    Performance Optimization    :p6d, 2026-11-11, 1w
```

### Phase Details

#### Phase 1: Features Only (MVP Enhancement)

**Goal**: Enhance current MVP with search and basic graph

```mermaid
graph TD
    P1["Phase 1: Features Only"]
    
    Search["Unified Search Box"]
    Fuzzy["Fuzzy Matching"]
    Typeahead["Type-ahead Suggestions"]
    
    Results["Search Results Sidebar"]
    Filters["Feature Filters"]
    
    Tree["Tree View"]
    FeatureDetails["Feature Details"]
    Dependencies["Dependency Links"]
    
    Graph["Basic Graph View"]
    FeatureNodes["Feature Nodes"]
    DepEdges["Dependency Edges"]
    
    P1 --> Search
    Search --> Fuzzy
    Search --> Typeahead
    
    P1 --> Results
    Results --> Filters
    
    P1 --> Tree
    Tree --> FeatureDetails
    Tree --> Dependencies
    
    P1 --> Graph
    Graph --> FeatureNodes
    Graph --> DepEdges
```

**Timeline**: 2-3 weeks

---

#### Phase 2: Add Bundles

**Goal**: Expand to show bundles and feature-bundle relationships

```mermaid
graph TD
    P2["Phase 2: Add Bundles"]
    
    BundleSearch["Bundle Search & Filtering"]
    FeatureBundleTree["Feature → Bundle Tree"]
    BundleFeatureTrace["Bundle → Feature Traceability"]
    BundleNodes["Bundle Nodes in Graph"]
    ContainmentEdges["Containment Edges"]
    
    P2 --> BundleSearch
    P2 --> FeatureBundleTree
    P2 --> BundleFeatureTrace
    P2 --> BundleNodes
    P2 --> ContainmentEdges
```

**Timeline**: 2-3 weeks

---

## Interaction Flow Diagrams

### Flow 1: Search and Explore Feature

```mermaid
sequenceDiagram
    actor User
    participant Search as Search Box
    participant API as Backend API
    participant Results as Results List
    participant Tree as Tree View
    participant Graph as Graph View
    
    User->>Search: Type "servlet"
    Search->>API: GET /api/search?q=servlet
    API-->>Search: Return suggestions
    Search-->>User: Show dropdown
    User->>Search: Select "servlet-6.0"
    Search->>Results: Update results
    Results-->>User: Show in list
    User->>Results: Click item
    Results->>Tree: Load feature details
    Tree-->>User: Display feature tree
    User->>Tree: Click "Show in Graph"
    Tree->>Graph: Switch to graph view
    Graph->>API: GET /api/graph/features?names=servlet-6.0
    API-->>Graph: Return graph data
    Graph-->>User: Display feature node
    User->>Graph: Double-click node
    Graph->>API: GET /api/features/servlet-6.0/dependencies
    API-->>Graph: Return dependencies
    Graph-->>User: Expand with dependency nodes
```

### Flow 2: Trace Class to Feature

```mermaid
sequenceDiagram
    actor User
    participant Search as Search Box
    participant Results as Results List
    participant Tree as Tree View
    participant Breadcrumb as Breadcrumb
    
    User->>Search: Type "ServletContext"
    Search-->>Results: Show class in results
    User->>Results: Click class
    Results->>Tree: Load class details
    Tree-->>User: Show class tree
    Note over Tree: Class Details<br/>Contained in Bundle: com.ibm.ws.servlet.3.1
    User->>Tree: Click bundle link
    Tree->>Tree: Navigate to bundle
    Tree-->>User: Show bundle tree
    Tree->>Breadcrumb: Update path
    Breadcrumb-->>User: Show: Home > ServletContext > com.ibm.ws.servlet.3.1
    Note over Tree: Bundle Details<br/>Contained in Features: servlet-6.0
    User->>Tree: Click feature link
    Tree->>Tree: Navigate to feature
    Tree-->>User: Show feature tree
    Tree->>Breadcrumb: Update path
    Breadcrumb-->>User: Show: Home > ServletContext > Bundle > servlet-6.0
```

### Flow 3: Filter and Visualize Graph

```mermaid
sequenceDiagram
    actor User
    participant Search as Search Box
    participant Filters as Filter Panel
    participant Graph as Graph View
    participant API as Backend API
    
    User->>Search: Type "servlet"
    Search-->>User: Show multiple results
    User->>Filters: Check "Features only"
    Filters-->>User: Results narrowed
    User->>Graph: Switch to Graph View
    Graph->>API: GET /api/graph/features?pattern=servlet*
    API-->>Graph: Return feature nodes
    Graph-->>User: Display feature nodes
    User->>Graph: Open filter panel
    User->>Filters: Check "Show Dependencies"
    Filters->>Graph: Update graph
    Graph->>API: GET dependencies for visible nodes
    API-->>Graph: Return dependency edges
    Graph-->>User: Display with edges
    User->>Graph: Select "Hierarchical" layout
    Graph-->>User: Reorganize graph
    User->>Graph: Double-click node
    Graph->>API: GET /api/features/{name}/dependencies
    API-->>Graph: Return related nodes
    Graph-->>User: Expand with new nodes
```

---

## Accessibility Requirements

### Colorblind-Friendly Design Strategy

```mermaid
graph TD
    Accessibility["Accessibility Strategy"]
    
    subgraph Visual["Visual Differentiation"]
        Icons["Unique Icons per Type"]
        Shapes["Different Node Shapes"]
        Patterns["Fill Patterns"]
        Borders["Border Styles"]
    end
    
    subgraph Text["Text Support"]
        Labels["Always Include Labels"]
        Tooltips["Descriptive Tooltips"]
        AltText["Alt Text for Icons"]
    end
    
    subgraph Keyboard["Keyboard Navigation"]
        TabNav["Tab Through Elements"]
        ArrowKeys["Arrow Keys for Trees"]
        Shortcuts["Keyboard Shortcuts"]
        Focus["Clear Focus Indicators"]
    end
    
    subgraph ScreenReader["Screen Reader Support"]
        ARIA["ARIA Labels"]
        LiveRegions["ARIA Live Regions"]
        Semantic["Semantic HTML"]
        Descriptions["Descriptive Link Text"]
    end
    
    Accessibility --> Visual
    Accessibility --> Text
    Accessibility --> Keyboard
    Accessibility --> ScreenReader
```

### Color and Pattern Combinations

```mermaid
graph LR
    subgraph ColorPatterns["Colorblind-Friendly Combinations"]
        F["Features<br/>Blue + Solid"]
        B["Bundles<br/>Green + Stripes"]
        C["Classes<br/>Orange + Dots"]
        D["DS Components<br/>Purple + Crosshatch"]
        E["Config<br/>Teal + Horizontal Lines"]
    end
    
    style F fill:#0f62fe,stroke:#000,stroke-width:3px
    style B fill:#24a148,stroke:#000,stroke-width:3px
    style C fill:#ff832b,stroke:#000,stroke-width:3px
    style D fill:#8a3ffc,stroke:#000,stroke-width:3px
    style E fill:#009d9a,stroke:#000,stroke-width:3px
```

---

## Carbon Design System Components

### Component Hierarchy

```mermaid
graph TD
    App["App Component"]
    
    subgraph Layout["Layout Components"]
        Header["Header"]
        Content["Content"]
        Grid["Grid"]
    end
    
    subgraph Sidebar["Sidebar Components"]
        Search["Search"]
        CheckboxGroup["Checkbox Group"]
        StructuredList["Structured List"]
    end
    
    subgraph MainArea["Main Area Components"]
        Breadcrumb["Breadcrumb"]
        ContentSwitcher["Content Switcher"]
        TreeView["Tree View Custom"]
        GraphView["Graph View Cytoscape"]
    end
    
    subgraph Common["Common Components"]
        Button["Button"]
        Tag["Tag"]
        Tooltip["Tooltip"]
        Loading["Loading"]
        Notification["Inline Notification"]
        Modal["Modal"]
        OverflowMenu["Overflow Menu"]
    end
    
    App --> Layout
    App --> Sidebar
    App --> MainArea
    App --> Common
```

### Required Carbon Components List

**Layout & Structure**:
- `Header`, `HeaderName`, `HeaderGlobalBar`, `HeaderGlobalAction`
- `Content`
- `Grid`, `Column`
- `Section`

**Navigation**:
- `Breadcrumb`, `BreadcrumbItem`
- `SideNav` (optional)

**Search & Filters**:
- `Search`
- `FilterableMultiSelect`
- `Checkbox`, `CheckboxGroup`
- `Dropdown`, `MultiSelect`
- `Toggle`

**Lists & Display**:
- `StructuredList`, `StructuredListWrapper`, `StructuredListHead`, `StructuredListBody`, `StructuredListRow`, `StructuredListCell`
- `DataTable` (for detailed views)
- `Tile`, `ClickableTile`
- `Tag`

**Controls**:
- `Button`
- `ContentSwitcher`, `Switch`
- `Tabs`, `Tab`
- `Slider`

**Feedback**:
- `Tooltip`
- `Loading`
- `InlineNotification`
- `Modal`

**Menus**:
- `OverflowMenu`, `OverflowMenuItem`

---

## Technical Implementation Notes

### State Management Structure

```mermaid
graph TD
    AppState["Application State"]
    
    subgraph SearchState["Search State"]
        Query["query: string"]
        Results["results: SearchResult[]"]
        Filters["filters: FilterState"]
    end
    
    subgraph NavigationState["Navigation State"]
        CurrentView["currentView: 'tree' | 'graph'"]
        SelectedItem["selectedItem: Item | null"]
        BreadcrumbPath["breadcrumb: BreadcrumbItem[]"]
    end
    
    subgraph GraphState["Graph State"]
        Nodes["nodes: Node[]"]
        Edges["edges: Edge[]"]
        Layout["layout: string"]
        GraphFilters["filters: GraphFilters"]
    end
    
    subgraph TreeState["Tree State"]
        ExpandedNodes["expandedNodes: Set<string>"]
        SelectedNode["selectedNode: string | null"]
    end
    
    AppState --> SearchState
    AppState --> NavigationState
    AppState --> GraphState
    AppState --> TreeState
```

### Performance Optimization Strategy

```mermaid
graph TD
    Performance["Performance Optimization"]
    
    subgraph Search["Search Optimization"]
        Debounce["Debounce Input 300ms"]
        LimitResults["Limit Results max 100 per type"]
        VirtualScroll["Virtual Scrolling"]
    end
    
    subgraph Graph["Graph Optimization"]
        LazyLoad["Lazy Load Nodes"]
        LimitNodes["Limit Visible Nodes max 500"]
        Progressive["Progressive Rendering"]
        WebWorkers["Web Workers for Layout"]
    end
    
    subgraph Tree["Tree Optimization"]
        LazyChildren["Lazy Load Children"]
        VirtualTree["Virtual Scrolling"]
        Memoization["Memoize Rendered Nodes"]
    end
    
    subgraph Caching["Caching Strategy"]
        APICache["Cache API Responses"]
        LocalStorage["Local Storage for Preferences"]
        SessionCache["Session Cache for Search"]
    end
    
    Performance --> Search
    Performance --> Graph
    Performance --> Tree
    Performance --> Caching
```

---

## API Integration

### Required API Endpoints

```mermaid
graph TD
    API["Backend API"]
    
    subgraph Phase1["Phase 1: Features"]
        SearchAPI["GET /api/search?q=query&types=features"]
        FeaturesAPI["GET /api/features"]
        FeatureDetailAPI["GET /api/features/{name}"]
        FeatureDepsAPI["GET /api/features/{name}/dependencies"]
        GraphFeaturesAPI["GET /api/graph/features?names=name1,name2"]
    end
    
    subgraph Phase2["Phase 2: Bundles"]
        BundlesAPI["GET /api/bundles"]
        BundleDetailAPI["GET /api/bundles/{name}"]
        BundleFeaturesAPI["GET /api/bundles/{name}/features"]
        GraphBundlesAPI["GET /api/graph/bundles?names=name1,name2"]
    end
    
    subgraph Phase3["Phase 3+: Classes, DS, Config"]
        ClassesAPI["GET /api/classes?bundle=name&package=name"]
        ClassDetailAPI["GET /api/classes/{fqn}"]
        DSAPI["GET /api/components"]
        ConfigAPI["GET /api/config/elements"]
    end
    
    API --> Phase1
    API --> Phase2
    API --> Phase3
```

---

## Summary

This interface design provides:

1. **Search-First Experience**: Unified search as primary interaction
2. **Progressive Disclosure**: Start simple (features), reveal complexity as needed
3. **Dual Views**: Tree for hierarchy, Graph for relationships
4. **Comprehensive Traceability**: Navigate up and down the stack
5. **Accessibility**: Colorblind-friendly with icons, patterns, and labels
6. **Phased Implementation**: Build incrementally from features to full system
7. **Carbon Design System**: Consistent IBM design language
8. **Flexible Filtering**: Control visibility at global and node level

The design balances power-user needs (comprehensive filtering, graph visualization) with ease of learning (simple search, progressive disclosure, helpful defaults).