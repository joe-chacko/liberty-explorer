/*
 * =============================================================================
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * =============================================================================
 */
package io.openliberty.inspect;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("unused")
enum Key implements Function<Attributes, String>, Predicate<Attributes> {
    CREATED_BY("Created-By"),
    IBM_API_PACKAGE("IBM-API-Package"),
    IBM_API_SERVICE("IBM-API-Service"),
    IBM_APP_FORCERESTART("IBM-App-ForceRestart"),
    IBM_APPLIESTO("IBM-AppliesTo"),
    IBM_FEATURE_VERSION("IBM-Feature-Version"),
    IBM_INSTALL_POLICY("IBM-Install-Policy"),
    IBM_INSTALLTO("IBM-InstallTo"),
    IBM_LICENSE_AGREEMENT("IBM-License-Agreement"),
    IBM_PROCESS_TYPES("IBM-Process-Types"),
    IBM_PRODUCTID("IBM-ProductID"),
    IBM_PROVISION_CAPABILITY("IBM-Provision-Capability"),
    IBM_SPI_PACKAGE("IBM-SPI-Package"),
    IBM_SHORTNAME("IBM-ShortName"),
    IBM_TEST_FEATURE("IBM-Test-Feature"),
    SUBSYSTEM_CATEGORY("Subsystem-Category"),
    SUBSYSTEM_CONTENT("Subsystem-Content"),
    SUBSYSTEM_DESCRIPTION("Subsystem-Description"),
    SUBSYSTEM_ENDPOINT_CONTENT("Subsystem-Endpoint-Content"),
    SUBSYSTEM_ENDPOINT_ICONS("Subsystem-Endpoint-Icons"),
    SUBSYSTEM_ENDPOINT_NAMES("Subsystem-Endpoint-Names"),
    SUBSYSTEM_ENDPOINT_SHORTNAMES("Subsystem-Endpoint-ShortNames"),
    SUBSYSTEM_ENDPOINT_URLS("Subsystem-Endpoint-Urls"),
    SUBSYSTEM_LICENSE("Subsystem-License"),
    SUBSYSTEM_LOCALIZATION("Subsystem-Localization"),
    SUBSYSTEM_MANIFESTVERSION("Subsystem-ManifestVersion"),
    SUBSYSTEM_NAME("Subsystem-Name"),
    SUBSYSTEM_SYMBOLICNAME("Subsystem-SymbolicName"),
    SUBSYSTEM_TYPE("Subsystem-Type"),
    SUBSYSTEM_VENDOR("Subsystem-Vendor"),
    SUBSYSTEM_VERSION("Subsystem-Version"),
    TOOL("Tool"),
    WLP_ACTIVATION_TYPE("WLP-Activation-Type");
    private static final Pattern ELEMENT_PATTERN = Pattern.compile("(([^\",\\\\]|\\\\.)+|\"([^\\\\\"]|\\\\.)*+\")+");
    final Attributes.Name name;

    Key(String name) {
        this.name = new Attributes.Name(name);
    }

    boolean isPresent(Attributes feature) {
        return feature.containsKey(name);
    }

    boolean isAbsent(Attributes feature) {
        return !isPresent(feature);
    }

    Optional<String> get(Attributes feature) {
        return Optional.ofNullable(feature.getValue(name));
    }

    Stream<ValueElement> parseValues(Attributes feature) {
        return get(feature)
                .map(ELEMENT_PATTERN::matcher)
                .map(Matcher::results)
                .orElse(Stream.empty())
                .map(MatchResult::group)
                .map(ValueElement::new);
    }

    public String apply(Attributes feature) {
        return feature.getValue(name);
    }

    public boolean test(Attributes feature) {
        return isPresent(feature);
    }
}
