import { useState, useEffect } from 'react'
import {
  Theme,
  Header,
  HeaderName,
  HeaderGlobalBar,
  HeaderGlobalAction,
  Content,
  Grid,
  Column,
  Tile,
  ClickableTile,
  Loading,
  InlineNotification,
  Button,
  Tag,
  StructuredListWrapper,
  StructuredListHead,
  StructuredListBody,
  StructuredListRow,
  StructuredListCell,
  Stack,
  Section,
  Search,
  Checkbox,
  CheckboxGroup,
} from '@carbon/react'
import { Rocket, Information } from '@carbon/icons-react'
import './App.css'

function App() {
  const [features, setFeatures] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [selectedFeature, setSelectedFeature] = useState(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [visibilityFilters, setVisibilityFilters] = useState({
    PUBLIC: true,
    PROTECTED: true,
    PRIVATE: true,
  })
  const [showAutoFeatures, setShowAutoFeatures] = useState(true)

  useEffect(() => {
    fetchFeatures()
  }, [])

  const fetchFeatures = async () => {
    try {
      setLoading(true)
      // Use relative path that works both in dev (with proxy) and production (deployed WAR)
      const apiUrl = import.meta.env.DEV ? '/api/features' : '../api/features'
      const response = await fetch(apiUrl)
      if (!response.ok) {
        throw new Error('Failed to fetch features')
      }
      const data = await response.json()
      setFeatures(data)
      setError(null)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleFeatureClick = (feature) => {
    setSelectedFeature(feature)
  }

  const handleVisibilityChange = (visibility) => {
    setVisibilityFilters((prev) => ({
      ...prev,
      [visibility]: !prev[visibility],
    }))
  }

  // Check if a feature is an auto-feature (typically has "auto" in symbolic name)
  const isAutoFeature = (feature) => {
    return feature.symbolicName.toLowerCase().includes('auto')
  }

  // Filter features based on search term, visibility, and auto-feature status
  const filteredFeatures = features.filter((feature) => {
    // Search filter
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase()
      const matchesSearch = (
        feature.name.toLowerCase().includes(searchLower) ||
        feature.symbolicName.toLowerCase().includes(searchLower) ||
        (feature.description && feature.description.toLowerCase().includes(searchLower))
      )
      if (!matchesSearch) return false
    }

    // Visibility filter
    if (feature.visibility && !visibilityFilters[feature.visibility]) {
      return false
    }

    // Auto-feature filter
    if (isAutoFeature(feature) && !showAutoFeatures) {
      return false
    }

    return true
  })

  return (
    <Theme theme="g10">
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

      <Content className="liberty-explorer-content">
        <Grid className="liberty-explorer-grid" fullWidth>
          <Column lg={16} md={8} sm={4} className="page-header">
            <Section level={1}>
              <div className="page-header__icon">
                <Rocket size={32} />
              </div>
              <h1 className="page-header__title">IBM Open Liberty Features</h1>
              <p className="page-header__description">
                Browse and analyze Liberty features, bundles, and their dependencies
              </p>
            </Section>
          </Column>

          <Column lg={6} md={4} sm={4} className="features-column">
            <Section level={2}>
              <h2 className="section-title">Features</h2>
              
              {!loading && !error && (
                <>
                  <Search
                    size="lg"
                    placeholder="Search features..."
                    labelText="Search features"
                    closeButtonLabelText="Clear search"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    onClear={() => setSearchTerm('')}
                    className="feature-search"
                  />
                  
                  <div className="feature-filters">
                    <CheckboxGroup legendText="Visibility">
                      <Checkbox
                        id="filter-public"
                        labelText="Public"
                        checked={visibilityFilters.PUBLIC}
                        onChange={() => handleVisibilityChange('PUBLIC')}
                      />
                      <Checkbox
                        id="filter-protected"
                        labelText="Protected"
                        checked={visibilityFilters.PROTECTED}
                        onChange={() => handleVisibilityChange('PROTECTED')}
                      />
                      <Checkbox
                        id="filter-private"
                        labelText="Private"
                        checked={visibilityFilters.PRIVATE}
                        onChange={() => handleVisibilityChange('PRIVATE')}
                      />
                    </CheckboxGroup>
                    
                    <CheckboxGroup legendText="Feature Type">
                      <Checkbox
                        id="filter-auto"
                        labelText="Auto-features"
                        checked={showAutoFeatures}
                        onChange={() => setShowAutoFeatures(!showAutoFeatures)}
                      />
                    </CheckboxGroup>
                  </div>
                </>
              )}
              
              {loading && (
                <div className="loading-container">
                  <Loading description="Loading features..." withOverlay={false} />
                </div>
              )}
              
              {error && (
                <InlineNotification
                  kind="error"
                  title="Error loading features"
                  subtitle={error}
                  lowContrast
                  onCloseButtonClick={() => setError(null)}
                  actions={
                    <Button size="sm" kind="tertiary" onClick={fetchFeatures}>
                      Retry
                    </Button>
                  }
                />
              )}
              
              {!loading && !error && (
                <>
                  {filteredFeatures.length === 0 ? (
                    <div className="no-results">
                      <p>No features found matching "{searchTerm}"</p>
                    </div>
                  ) : (
                    <Stack gap={3} className="features-list">
                      {filteredFeatures.map((feature) => (
                        <ClickableTile
                          key={feature.symbolicName}
                          onClick={() => handleFeatureClick(feature)}
                          className={`feature-tile ${
                            selectedFeature?.symbolicName === feature.symbolicName ? 'selected' : ''
                          }`}
                        >
                          <div className="feature-tile__content">
                            <h4 className="feature-tile__name">{feature.name}</h4>
                            <p className="feature-tile__symbolic-name">
                              {feature.symbolicName}
                            </p>
                            <div className="feature-tile__tags">
                              <Tag type="blue" size="sm">v{feature.version}</Tag>
                              {feature.visibility && (
                                <Tag
                                  type={feature.visibility === 'PUBLIC' ? 'green' : 'gray'}
                                  size="sm"
                                >
                                  {feature.visibility}
                                </Tag>
                              )}
                            </div>
                          </div>
                        </ClickableTile>
                      ))}
                    </Stack>
                  )}
                </>
              )}
            </Section>
          </Column>

          {selectedFeature && (
            <Column lg={10} md={4} sm={4} className="details-column">
              <Section level={2}>
                <h2 className="section-title">Feature Details</h2>
                <Tile className="details-tile">
                  <Stack gap={6}>
                    <div className="details-header">
                      <h3 className="details-title">{selectedFeature.name}</h3>
                      <div className="details-tags">
                        <Tag type="blue">v{selectedFeature.version}</Tag>
                        {selectedFeature.visibility && (
                          <Tag type={selectedFeature.visibility === 'PUBLIC' ? 'green' : 'gray'}>
                            {selectedFeature.visibility}
                          </Tag>
                        )}
                      </div>
                    </div>
                    
                    <StructuredListWrapper className="details-list">
                      <StructuredListHead>
                        <StructuredListRow head>
                          <StructuredListCell head>Property</StructuredListCell>
                          <StructuredListCell head>Value</StructuredListCell>
                        </StructuredListRow>
                      </StructuredListHead>
                      <StructuredListBody>
                        <StructuredListRow>
                          <StructuredListCell>Symbolic Name</StructuredListCell>
                          <StructuredListCell>{selectedFeature.symbolicName}</StructuredListCell>
                        </StructuredListRow>
                        <StructuredListRow>
                          <StructuredListCell>Version</StructuredListCell>
                          <StructuredListCell>{selectedFeature.version}</StructuredListCell>
                        </StructuredListRow>
                        <StructuredListRow>
                          <StructuredListCell>Visibility</StructuredListCell>
                          <StructuredListCell>
                            <Tag type={selectedFeature.visibility === 'PUBLIC' ? 'green' : 'gray'}>
                              {selectedFeature.visibility}
                            </Tag>
                          </StructuredListCell>
                        </StructuredListRow>
                        {selectedFeature.description && (
                          <StructuredListRow>
                            <StructuredListCell>Description</StructuredListCell>
                            <StructuredListCell>{selectedFeature.description}</StructuredListCell>
                          </StructuredListRow>
                        )}
                        {selectedFeature.dependencies && selectedFeature.dependencies.length > 0 && (
                          <StructuredListRow>
                            <StructuredListCell>Dependencies</StructuredListCell>
                            <StructuredListCell>
                              <ul className="dependencies-list">
                                {selectedFeature.dependencies.map((dep, idx) => (
                                  <li key={idx}>{dep}</li>
                                ))}
                              </ul>
                            </StructuredListCell>
                          </StructuredListRow>
                        )}
                      </StructuredListBody>
                    </StructuredListWrapper>
                  </Stack>
                </Tile>
              </Section>
            </Column>
          )}
        </Grid>
      </Content>
    </Theme>
  )
}

export default App

// Made with Bob
