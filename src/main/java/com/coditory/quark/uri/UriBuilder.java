package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coditory.quark.uri.Ports.SCHEME_DEFAULT_PORT_NUMBER;
import static com.coditory.quark.uri.Ports.validatePortNumberOrSchemeDefault;
import static com.coditory.quark.uri.Preconditions.expectNonBlank;
import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static com.coditory.quark.uri.Strings.emptyToNull;
import static com.coditory.quark.uri.Strings.isNotNullOrBlank;
import static com.coditory.quark.uri.Strings.isNotNullOrEmpty;
import static com.coditory.quark.uri.Strings.isNullOrEmpty;
import static com.coditory.quark.uri.Strings.lowerCase;
import static com.coditory.quark.uri.UriComponentsParser.parseQuery;
import static com.coditory.quark.uri.UriRfc.PATH_SEGMENT;

public final class UriBuilder {
    @NotNull
    public static UriBuilder from(@NotNull UriComponents uriComponents) {
        expectNonNull(uriComponents, "uriComponents");
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
            builder.setQueryMultiParams(uriComponents.getQueryParams());
        }
        return builder;
    }

    @NotNull
    public static UriBuilder from(@NotNull URI uri) {
        expectNonNull(uri, "uri");
        return new UriBuilder().setUri(uri);
    }

    @NotNull
    public static UriBuilder from(@NotNull URL url) {
        expectNonNull(url, "url");
        URI uri;
        try {
            uri = url.toURI();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert url to uri", e);
        }
        return new UriBuilder().setUri(uri);
    }

    @NotNull
    public static UriBuilder fromUri(@NotNull String uri) {
        expectNonNull(uri, "uri");
        return UriComponentsParser.parseUri(uri);
    }

    @Nullable
    public static UriBuilder fromUriOrNull(@Nullable String uri) {
        if (uri == null) {
            return null;
        }
        try {
            return fromUri(uri);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @NotNull
    public static UriBuilder fromHttpUrl(@NotNull String url) {
        expectNonNull(url, "url");
        return UriComponentsParser.parseHttpUrl(url);
    }

    @Nullable
    public static UriBuilder fromHttpUrlOrNull(@Nullable String url) {
        if (url == null) {
            return null;
        }
        try {
            return fromHttpUrl(url);
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
                setQuery(uri.getRawQuery());
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
        return from(buildUriComponents());
    }

    @NotNull
    public UriComponents buildUriComponents() {
        return this.ssp != null
                ? UriComponents.buildOpaque(scheme, ssp, fragment)
                : UriComponents.buildHierarchical(scheme, userInfo, host, port, protocolRelative, rootPath, pathSegments, queryParams, fragment);
    }

    @NotNull
    public URI buildUri() {
        return buildUriComponents().toUri();
    }

    @NotNull
    public String buildUriString() {
        return buildUriComponents().toUriString();
    }

    @NotNull
    public UriBuilder setScheme(@Nullable String scheme) {
        if (isNotNullOrBlank(scheme)) {
            if (scheme.equals("//")) {
                this.scheme = null;
                this.protocolRelative = true;
            } else {
                this.scheme = lowerCase(scheme);
                this.protocolRelative = false;
            }
            resetSchemeSpecificPart();
        } else {
            this.scheme = null;
        }
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
    public UriBuilder setSchemeSpecificPart(@Nullable String ssp) {
        if (isNotNullOrBlank(ssp)) {
            this.ssp = ssp;
            resetHierarchicalComponents();
        } else {
            this.ssp = null;
        }
        return this;
    }

    @NotNull
    public UriBuilder removeSchemeSpecificPart() {
        this.ssp = null;
        return this;
    }

    @NotNull
    public UriBuilder setUserInfo(@Nullable String userInfo) {
        if (isNotNullOrBlank(userInfo)) {
            this.userInfo = userInfo;
            resetSchemeSpecificPart();
        } else {
            this.userInfo = null;
        }
        return this;
    }

    @NotNull
    public UriBuilder removeUserInfo() {
        this.userInfo = null;
        return this;
    }

    @NotNull
    public UriBuilder setHost(@Nullable String host) {
        if (isNotNullOrBlank(host)) {
            this.host = lowerCase(host);
            this.rootPath = true;
            resetSchemeSpecificPart();
        } else {
            this.host = null;
        }
        return this;
    }

    @NotNull
    public UriBuilder removeHost() {
        this.host = null;
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
    public UriBuilder setPath(@Nullable String path) {
        this.pathSegments.clear();
        this.rootPath = this.host != null;
        addSubPath(path);
        return this;
    }

    @NotNull
    public UriBuilder addSubPath(@Nullable String subPath) {
        if (isNotNullOrEmpty(subPath)) {
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
    public UriBuilder setPathSegments(@NotNull List<String> pathSegments) {
        expectNonNull(pathSegments, "pathSegments");
        this.pathSegments.clear();
        addPathSegments(pathSegments);
        return this;
    }

    @NotNull
    public UriBuilder addPathSegment(@Nullable String pathSegment) {
        if (isNullOrEmpty(pathSegment)) {
            return this;
        }
        addPathSegments(List.of(pathSegment));
        return this;
    }

    @NotNull
    public UriBuilder addPathSegments(@Nullable List<String> pathSegments) {
        expectNonNull(pathSegments, "pathSegments");
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
    public UriBuilder setQuery(@NotNull String query) {
        expectNonNull(query, "query");
        setQueryMultiParams(parseQuery(query));
        return this;
    }

    @NotNull
    public UriBuilder setQueryParams(@NotNull Map<String, String> params) {
        expectNonNull(params, "params");
        this.queryParams.clear();
        params.forEach(this::putQueryParam);
        return this;
    }

    @NotNull
    public UriBuilder setQueryMultiParams(@NotNull Map<String, List<String>> params) {
        expectNonNull(params, "params");
        this.queryParams.clear();
        params.forEach(this::putQueryParam);
        return this;
    }

    @NotNull
    public UriBuilder putQueryParams(@NotNull Map<String, String> params) {
        expectNonNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotNullOrBlank(e.getKey()))
                .forEach(entry -> putQueryParam(entry.getKey(), entry.getValue()));
        return this;
    }

    @NotNull
    public UriBuilder putQueryMultiParams(@NotNull Map<String, List<String>> params) {
        expectNonNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotNullOrBlank(e.getKey()))
                .filter(e -> e.getValue() != null)
                .forEach(entry -> putQueryParam(entry.getKey(), entry.getValue()));
        return this;
    }

    @NotNull
    public UriBuilder putQueryParam(@NotNull String name, @NotNull String value) {
        expectNonBlank(name, "name");
        expectNonNull(value, "value");
        List<String> values = new ArrayList<>();
        values.add(value);
        return putQueryParam(name, values);
    }

    @NotNull
    public UriBuilder putQueryParam(@NotNull String name, @NotNull Collection<String> values) {
        expectNonBlank(name, "name");
        expectNonNull(values, "values");
        if (values.isEmpty()) {
            this.queryParams.remove(name);
        } else {
            this.queryParams.put(name, new ArrayList<>(values));
        }
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder addQueryParams(@NotNull Map<String, String> params) {
        expectNonNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotNullOrBlank(e.getKey()))
                .forEach(entry -> addQueryParam(entry.getKey(), entry.getValue()));
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder addQueryMultiParams(@NotNull Map<String, List<String>> params) {
        expectNonNull(params, "params");
        params.entrySet().stream()
                .filter(e -> isNotNullOrBlank(e.getKey()))
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .forEach(entry -> addQueryParam(entry.getKey(), entry.getValue()));
        resetSchemeSpecificPart();
        return this;
    }

    @NotNull
    public UriBuilder addQueryParam(@NotNull String name, @NotNull String value) {
        expectNonBlank(name, "name");
        expectNonNull(value, "value");
        return addQueryParam(name, List.of(value));
    }

    @NotNull
    public UriBuilder addQueryParam(@NotNull String name, @NotNull Collection<String> values) {
        expectNonBlank(name, "name");
        expectNonNull(values, "values");
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
    public UriBuilder removeQueryParam(@NotNull String name) {
        expectNonNull(name, "name");
        this.queryParams.remove(name);
        return this;
    }

    @NotNull
    public UriBuilder removeQueryParam(@NotNull String name, @Nullable String value) {
        expectNonNull(name, "name");
        List<String> values = this.queryParams.get(name);
        if (values != null && value != null) {
            values.remove(value);
        }
        return this;
    }

    @NotNull
    public UriBuilder setFragment(@Nullable String fragment) {
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
        buildUriComponents();
    }
}
