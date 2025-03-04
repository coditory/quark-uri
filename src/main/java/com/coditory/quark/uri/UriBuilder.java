package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coditory.quark.uri.Ports.SCHEME_DEFAULT_PORT_NUMBER;
import static com.coditory.quark.uri.Ports.validatePortNumberOrSchemeDefault;
import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static com.coditory.quark.uri.Strings.emptyToNull;
import static com.coditory.quark.uri.Strings.isNotNullOrEmpty;
import static com.coditory.quark.uri.Strings.lowerCase;
import static com.coditory.quark.uri.UriComponentsParser.parseQuery;
import static com.coditory.quark.uri.UriRfc.PATH_SEGMENT;

public final class UriBuilder {
    @NotNull
    public static UriBuilder from(UriComponents uriComponents) {
        if (uriComponents == null) return empty();
        UriBuilder builder = new UriBuilder();
        builder.scheme = uriComponents.getScheme();
        builder.ssp = uriComponents.getSchemeSpecificPart();
        builder.fragment = uriComponents.getFragment();
        if (!uriComponents.isOpaque()) {
            builder.userInfo = uriComponents.getUserInfo();
            builder.protocolRelative = uriComponents.isProtocolRelative();
            builder.host = uriComponents.getHost();
            builder.port = uriComponents.getPort();
            builder.setRootPath(uriComponents.isRootPath());
            builder.setPathSegments(uriComponents.getPathSegments());
            builder.setQueryMultiParams(uriComponents.getQueryMultiParams());
        }
        return builder;
    }

    @NotNull
    public static UriBuilder from(URI uri) {
        if (uri == null) return empty();
        return new UriBuilder().setUri(uri);
    }

    @NotNull
    public static UriBuilder from(URL url) {
        if (url == null) return empty();
        URI uri;
        try {
            uri = url.toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert url to uri", e);
        }
        return new UriBuilder().setUri(uri);
    }

    @NotNull
    public static UriBuilder fromUri(String uri) {
        if (uri == null) return empty();
        return UriComponentsParser.parseUri(uri);
    }

    @NotNull
    public static UriBuilder empty() {
        return new UriBuilder();
    }

    @Nullable
    public static UriBuilder fromUriOrNull(String uri) {
        if (uri == null) return null;
        try {
            return fromUri(uri);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @NotNull
    public static UriBuilder fromUrl(String url) {
        if (url == null) return empty();
        return UriComponentsParser.parseUrl(url);
    }

    @Nullable
    public static UriBuilder fromUrlOrNull(String url) {
        if (url == null) return null;
        try {
            return fromUrl(url);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @NotNull
    public static UriBuilder fromQueryString(String query) {
        if (query == null) return empty();
        Map<String, List<String>> params = UriComponentsParser.parseQuery(query);
        return new UriBuilder().setQueryMultiParams(params);
    }

    @Nullable
    public static UriBuilder fromQueryStringOrNull(String query) {
        if (query == null) return null;
        try {
            return fromQueryString(query);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String scheme;
    private String ssp;
    private String userInfo;
    private String host;
    private int port = SCHEME_DEFAULT_PORT_NUMBER;
    private boolean protocolRelative = false;
    private boolean rootPath = false;
    private final List<String> pathSegments = new ArrayList<>();
    private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
    private String fragment;

    UriBuilder() {
    }

    @NotNull
    public UriBuilder setUri(@NotNull URI uri) {
        expectNonNull(uri, "uri");
        this.scheme = uri.getScheme();
        if (uri.isOpaque()) {
            this.ssp = uri.getRawSchemeSpecificPart();
            resetHierarchicalComponents();
        } else {
            if (uri.getRawUserInfo() != null) {
                this.userInfo = uri.getRawUserInfo();
            }
            if (uri.getHost() != null) {
                this.host = uri.getHost();
            }
            if (uri.getPort() != -1) {
                this.port = uri.getPort();
            }
            if (isNotNullOrEmpty(uri.getRawPath())) {
                setPath(uri.getRawPath());
            }
            if (isNotNullOrEmpty(uri.getRawQuery())) {
                this.queryParams.clear();
                setQueryString(uri.getRawQuery());
            }
            resetSchemeSpecificPart();
        }
        if (uri.getRawFragment() != null) {
            this.fragment = uri.getRawFragment();
        }
        return this;
    }

    @NotNull
    public UriBuilder copy() {
        return from(toUriComponents());
    }

    @NotNull
    public UriComponents toUriComponents() {
        return this.ssp != null
                ? UriComponents.buildOpaque(scheme, ssp, fragment)
                : UriComponents.buildHierarchical(scheme, userInfo, host, port, protocolRelative, rootPath, pathSegments, queryParams, fragment);
    }

    @NotNull
    public URI toUri() {
        return toUriComponents().toUri();
    }

    @NotNull
    public String toUriString() {
        return toUriComponents().toUriString();
    }

    @Override
    @NotNull
    public String toString() {
        return toUriString();
    }

    @NotNull
    public UriBuilder setScheme(String scheme) {
        if (scheme == null || scheme.isBlank()) {
            this.scheme = null;
        } else if (scheme.equals("//")) {
            this.scheme = null;
            this.protocolRelative = true;
        } else {
            this.scheme = lowerCase(scheme);
            this.protocolRelative = false;
        }
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder setProtocolRelative(boolean protocolRelative) {
        this.scheme = null;
        this.protocolRelative = protocolRelative;
        return this;
    }

    @NotNull
    public UriBuilder removeScheme() {
        this.scheme = null;
        return this;
    }

    @NotNull
    public UriBuilder setSchemeSpecificPart(String ssp) {
        if (ssp == null || ssp.isBlank()) {
            this.ssp = null;
        } else {
            this.ssp = ssp;
        }
        resetHierarchicalComponents();
        return this;
    }

    @NotNull
    public UriBuilder removeSchemeSpecificPart() {
        this.ssp = null;
        resetHierarchicalComponents();
        return this;
    }

    @NotNull
    public UriBuilder setUserInfo(@Nullable String userInfo) {
        if (userInfo == null || userInfo.isBlank()) {
            this.userInfo = null;
        } else {
            this.userInfo = userInfo;
        }
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder removeUserInfo() {
        this.userInfo = null;
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder setHost(String host) {
        if (host == null || host.isBlank()) {
            this.host = null;
        } else {
            this.host = lowerCase(host);
            this.rootPath = true;
        }
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder removeHost() {
        this.host = null;
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder setDefaultPort() {
        this.port = -1;
        return this;
    }

    @NotNull
    public UriBuilder setPort(int port) {
        validatePortNumberOrSchemeDefault(port);
        this.port = port;
        if (port > -1) {
            resetSchemeSpecificPart();
        }
        return this;
    }

    @NotNull
    public UriBuilder setPath(String path) {
        this.pathSegments.clear();
        this.rootPath = this.host != null;
        addSubPath(path);
        return this;
    }

    @NotNull
    public UriBuilder addSubPath(String subPath) {
        if (subPath == null || subPath.isBlank()) {
            return this;
        }
        if (this.pathSegments.isEmpty()) {
            rootPath = subPath.startsWith("/") || this.host != null;
        }
        List<String> newSegments = Arrays.stream(subPath.split("/"))
                .filter(Strings::isNotNullOrEmpty)
                .map(PATH_SEGMENT::validateAndDecode)
                .toList();
        this.pathSegments.addAll(newSegments);
        if (!newSegments.isEmpty()) {
            resetSchemeSpecificPart();
        }
        return this;
    }

    @NotNull
    public UriBuilder setRootPath(boolean rootPath) {
        this.rootPath = rootPath;
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder setPathSegments(List<String> pathSegments) {
        this.pathSegments.clear();
        if (pathSegments != null && !pathSegments.isEmpty()) {
            addPathSegments(pathSegments);
        }
        return this;
    }

    @NotNull
    public UriBuilder addPathSegment(String pathSegment) {
        if (pathSegment == null || pathSegment.isEmpty()) return this;
        addPathSegments(List.of(pathSegment));
        return this;
    }

    @NotNull
    public UriBuilder addPathSegments(List<String> pathSegments) {
        if (pathSegments == null || pathSegments.isEmpty()) return this;
        List<String> filtered = pathSegments.stream()
                .filter(Strings::isNotNullOrEmpty)
                .toList();
        this.pathSegments.addAll(filtered);
        if (!filtered.isEmpty()) {
            resetSchemeSpecificPart();
        }
        return this;
    }

    @NotNull
    public UriBuilder setQueryString(String query) {
        if (query == null || query.isBlank()) {
            this.queryParams.clear();
            return this;
        }
        setQueryMultiParams(parseQuery(query));
        return this;
    }

    @NotNull
    public UriBuilder setQueryParams(Map<String, String> params) {
        this.queryParams.clear();
        if (params == null || params.isEmpty()) return this;
        params.forEach(this::putQueryParam);
        return this;
    }

    @NotNull
    public UriBuilder setQueryMultiParams(Map<String, List<String>> params) {
        this.queryParams.clear();
        if (params == null || params.isEmpty()) return this;
        params.forEach(this::putQueryMultiParam);
        return this;
    }

    @NotNull
    public UriBuilder putQueryParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) return this;
        params.forEach(this::putQueryParam);
        return this;
    }

    @NotNull
    public UriBuilder putQueryMultiParams(Map<String, List<String>> params) {
        if (params == null || params.isEmpty()) return this;
        params.forEach(this::putQueryMultiParam);
        return this;
    }

    @NotNull
    public UriBuilder putQueryParam(String name, String value) {
        if (name == null || name.isBlank()) return this;
        if (value == null) return this;
        List<String> values = new ArrayList<>();
        values.add(value);
        return putQueryMultiParam(name, values);
    }

    @NotNull
    public UriBuilder putQueryMultiParam(String name, Collection<String> values) {
        if (name == null || name.isBlank()) return this;
        if (values == null || values.isEmpty()) {
            this.queryParams.remove(name);
        } else {
            this.queryParams.put(name, new ArrayList<>(values));
        }
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder addQueryParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) return this;
        params.forEach(this::addQueryParam);
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder addQueryMultiParams(Map<String, List<String>> params) {
        if (params == null || params.isEmpty()) return this;
        params.forEach(this::addQueryMultiParam);
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder addQueryParam(String name, String value) {
        if (name == null || name.isBlank()) return this;
        if (value == null) return this;
        return addQueryMultiParam(name, List.of(value));
    }

    @NotNull
    public UriBuilder addQueryMultiParam(String name, Collection<String> values) {
        if (name == null || name.isBlank()) return this;
        if (values == null || values.isEmpty()) return this;
        List<String> noNullValues = values.stream()
                .filter(Objects::nonNull)
                .toList();
        if (noNullValues.isEmpty()) {
            return this;
        }
        this.queryParams.compute(name, (key, prevValues) -> {
            List<String> result = prevValues != null
                    ? prevValues
                    : new ArrayList<>();
            result.addAll(noNullValues);
            return result;
        });
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder removeQueryParams() {
        this.queryParams.clear();
        return this;
    }

    @NotNull
    public UriBuilder removeQueryParam(String name) {
        if (name == null || name.isBlank()) return this;
        this.queryParams.remove(name);
        return this;
    }

    @NotNull
    public UriBuilder removeQueryParam(String name, String value) {
        if (name == null || name.isBlank()) return this;
        if (value == null) return this;
        List<String> values = this.queryParams.get(name);
        if (values != null) {
            values.remove(value);
        }
        return this;
    }

    @NotNull
    public UriBuilder sortQueryParams() {
        Map<String, List<String>> copy = new HashMap<>(this.queryParams);
        this.queryParams.clear();
        copy.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> this.queryParams.put(e.getKey(), e.getValue()));
        return this;
    }

    @NotNull
    public UriBuilder sortQueryParamValues() {
        this.queryParams.forEach((key, value) -> Collections.sort(value));
        return this;
    }

    @NotNull
    public UriBuilder sortQueryParamsAndValues() {
        sortQueryParamValues();
        sortQueryParams();
        return this;
    }

    @NotNull
    public UriBuilder setFragment(String fragment) {
        this.fragment = emptyToNull(fragment);
        return this;
    }

    @NotNull
    public UriBuilder removeFragment() {
        this.fragment = null;
        return this;
    }

    private void resetHierarchicalComponents() {
        this.userInfo = null;
        this.host = null;
        this.port = SCHEME_DEFAULT_PORT_NUMBER;
        this.pathSegments.clear();
        this.queryParams.clear();
    }

    private void resetSchemeSpecificPart() {
        this.ssp = null;
    }

    void validate() {
        toUriComponents();
    }
}
