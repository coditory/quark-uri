package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.coditory.quark.uri.Nullable.onNotNull;
import static com.coditory.quark.uri.Strings.isNotNullOrEmpty;
import static com.coditory.quark.uri.Strings.isNullOrEmpty;
import static com.coditory.quark.uri.UriPartValidator.checkPort;
import static com.coditory.quark.uri.UriRfc.FRAGMENT;
import static com.coditory.quark.uri.UriRfc.PATH_SEGMENT;
import static com.coditory.quark.uri.UriRfc.QUERY_PARAM_NARROW;
import static com.coditory.quark.uri.UriRfc.SCHEME;
import static com.coditory.quark.uri.UriRfc.SCHEME_SPECIFIC_PART;
import static com.coditory.quark.uri.UriRfc.USER_INFO;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public final class UriComponents {
    @NotNull
    public static UriComponents empty() {
        return UriBuilder.empty().toUriComponents();
    }

    @NotNull
    public static UriComponents fromUri(String uri) {
        if (uri == null || uri.isBlank()) return empty();
        return builderFromUri(uri).toUriComponents();
    }

    @Nullable
    public static UriComponents fromUriOrNull(String uri) {
        if (uri == null) return null;
        try {
            return fromUri(uri);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @NotNull
    public static UriComponents fromHttpUrl(String url) {
        if (url == null || url.isBlank()) return empty();
        return builderFromHttpUrl(url).toUriComponents();
    }

    @Nullable
    static UriComponents fromHttpUrlOrNull(String url) {
        if (url == null) return null;
        try {
            return fromHttpUrl(url);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    public static UriComponents fromQueryStringOrNull(String query) {
        if (query == null) return null;
        try {
            return fromQueryString(query);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @NotNull
    public static UriComponents fromQueryString(String query) {
        if (query == null) return empty();
        return builderFromQueryString(query).toUriComponents();
    }

    @NotNull
    public static UriComponents from(URI uri) {
        if (uri == null) return empty();
        return builderFrom(uri).toUriComponents();
    }

    @NotNull
    public static UriComponents from(URL url) {
        if (url == null) return empty();
        return builderFrom(url).toUriComponents();
    }

    @NotNull
    public static UriBuilder builder() {
        return new UriBuilder();
    }

    @NotNull
    public static UriBuilder builderFrom(UriComponents uriComponents) {
        return UriBuilder.from(uriComponents);
    }

    @NotNull
    public static UriBuilder builderFrom(URL url) {
        return UriBuilder.from(url);
    }

    @NotNull
    public static UriBuilder builderFrom(URI uri) {
        return UriBuilder.from(uri);
    }

    @NotNull
    public static UriBuilder builderFromUri(String uri) {
        return UriBuilder.fromUri(uri);
    }

    @NotNull
    public static UriBuilder builderFromHttpUrl(String url) {
        return UriBuilder.fromUrl(url);
    }

    @NotNull
    public static UriBuilder builderFromQueryString(String query) {
        return UriBuilder.fromQueryString(query);
    }

    static UriComponents buildOpaque(
            String scheme,
            String ssp,
            String fragment
    ) {
        return new UriComponents(scheme, ssp, null, null, -1, false, false, null, null, fragment);
    }

    static UriComponents buildHierarchical(
            String scheme,
            String userInfo,
            String host,
            int port,
            boolean protocolRelative,
            boolean rootRelative,
            List<String> pathSegments,
            Map<String, List<String>> queryParams,
            String fragment
    ) {
        if (isNullOrEmpty(host)) {
            if (isNotNullOrEmpty(userInfo)) {
                throw new InvalidUriException("URI with user info must include host");
            }
            if (port >= 0) {
                throw new InvalidUriException("URI with port must include host");
            }
        }
        if (scheme != null && protocolRelative) {
            throw new InvalidUriException("URI cannot be protocol relative and have a scheme");
        }
        onNotNull(scheme, UriPartValidator::checkScheme);
        onNotNull(host, UriPartValidator::checkHost);
        if (port >= 0) {
            checkPort(port);
        }
        return new UriComponents(scheme, null, userInfo, host, port, protocolRelative, rootRelative, pathSegments, queryParams, fragment);
    }

    private final String ssp;
    private final String scheme;
    private final String userInfo;
    private final String host;
    private final int port;
    private final boolean protocolRelative;
    private final boolean rootPath;
    private final List<String> pathSegments;
    private final Map<String, List<String>> queryParams;
    private final String fragment;

    private UriComponents(
            String scheme,
            String ssp,
            String userInfo,
            String host,
            int port,
            boolean protocolRelative,
            boolean rootPath, List<String> pathSegments,
            Map<String, List<String>> queryParams,
            String fragment
    ) {
        this.scheme = scheme;
        this.ssp = ssp;
        this.userInfo = userInfo;
        this.host = host;
        this.port = port;
        this.protocolRelative = protocolRelative;
        this.rootPath = rootPath;
        this.pathSegments = pathSegments == null ? List.of() : List.copyOf(pathSegments);
        this.queryParams = queryParams == null ? Map.of() : unmodifiableMap(new LinkedHashMap<>(queryParams));
        this.fragment = fragment;
    }

    public boolean isOpaque() {
        return ssp != null;
    }

    @Nullable
    public String getSchemeSpecificPart() {
        return ssp;
    }

    @Nullable
    public String getScheme() {
        return scheme;
    }

    @Nullable
    public String getUserInfo() {
        return userInfo;
    }

    @Nullable
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isRootPath() {
        return rootPath;
    }

    public boolean isProtocolRelative() {
        return protocolRelative;
    }

    @NotNull
    public List<String> getPathSegments() {
        return pathSegments;
    }

    @NotNull
    public Map<String, List<String>> getQueryMultiParams() {
        return queryParams;
    }

    @NotNull
    public Map<String, String> getQueryParams() {
        return queryParams.entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().getFirst()));
    }

    @Nullable
    public List<String> getQueryMultiParam(String name) {
        if (name == null) return null;
        return queryParams.get(name);
    }

    @Nullable
    public String getQueryParam(String name) {
        if (name == null) return null;
        List<String> values = queryParams.get(name);
        return (values != null && !values.isEmpty()) ? values.getFirst() : null;
    }

    @Nullable
    public UriAuthority getUriAuthority() {
        if (isOpaque()) {
            return null;
        }
        UriAuthority authority = UriAuthority.of(userInfo, host, port);
        return authority.isEmpty() ? null : authority;
    }

    @Nullable
    public String getQueryString() {
        if (this.queryParams.isEmpty()) {
            return null;
        }
        StringBuilder queryBuilder = new StringBuilder();
        this.queryParams.forEach((name, values) -> {
            if (values == null || values.isEmpty()) {
                if (!queryBuilder.isEmpty()) {
                    queryBuilder.append('&');
                }
                queryBuilder.append(QUERY_PARAM_NARROW.encode(name));
            } else {
                for (Object value : values) {
                    if (!queryBuilder.isEmpty()) {
                        queryBuilder.append('&');
                    }
                    queryBuilder.append(QUERY_PARAM_NARROW.encode(name))
                            .append('=')
                            .append(QUERY_PARAM_NARROW.encode(value.toString()));
                }
            }
        });
        return queryBuilder.toString();
    }

    @Nullable
    public String getPath() {
        if (pathSegments.isEmpty()) {
            return null;
        }
        String path = pathSegments.stream()
                .map(PATH_SEGMENT::encode)
                .collect(joining("/"));
        String prefix = rootPath ? "/" : "";
        return prefix + path;
    }

    @Nullable
    public String getFragment() {
        return fragment;
    }

    public boolean isHttpUrl() {
        return !isOpaque() && ("http".equals(scheme) || "https".equals(scheme));
    }

    public boolean isValidHttpUrl() {
        return UrlValidator.isValidUrl(this);
    }

    @NotNull
    public URL toUrl() {
        try {
            return URI.create(toUriString()).toURL();
        } catch (Exception e) {
            throw new IllegalStateException("Could not build URI", e);
        }
    }

    @NotNull
    public URI toUri() {
        try {
            return new URI(toUriString());
        } catch (Exception e) {
            throw new IllegalStateException("Could not build URI", e);
        }
    }

    @NotNull
    public String toUriString() {
        return ssp != null
                ? toOpaqueUriString()
                : toHierarchicalUriString();
    }

    private String toOpaqueUriString() {
        StringBuilder uriBuilder = new StringBuilder();
        if (scheme != null) {
            uriBuilder.append(SCHEME.encode(scheme))
                    .append(':');
        }
        uriBuilder.append(SCHEME_SPECIFIC_PART.encode(ssp));
        if (fragment != null) {
            uriBuilder.append('#')
                    .append(FRAGMENT.encode(fragment));
        }
        return uriBuilder.toString();
    }

    private String toHierarchicalUriString() {
        StringBuilder uriBuilder = new StringBuilder();
        if (scheme != null) {
            uriBuilder.append(SCHEME.encode(scheme))
                    .append("://");
        } else if (protocolRelative) {
            uriBuilder.append("//");
        }
        if (userInfo != null || host != null) {
            if (userInfo != null) {
                uriBuilder.append(USER_INFO.encode(this.userInfo))
                        .append('@');
            }
            if (host != null) {
                uriBuilder.append(this.host);
            }
            if (port != -1) {
                uriBuilder.append(':')
                        .append(this.port);
            }
        }
        String path = getPath();
        String query = getQueryString();
        if (path != null) {
            if (!path.equals("/") || query != null || fragment != null || host == null) {
                uriBuilder.append(path);
            }
        } else if (rootPath && host == null) {
            uriBuilder.append("/");
        }
        if (query != null) {
            uriBuilder.append('?')
                    .append(query);
        }
        if (fragment != null) {
            uriBuilder.append('#')
                    .append(FRAGMENT.encode(fragment));
        }
        return uriBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UriComponents that = (UriComponents) o;
        return port == that.port
                && protocolRelative == that.protocolRelative
                && rootPath == that.rootPath
                && Objects.equals(ssp, that.ssp)
                && Objects.equals(scheme, that.scheme)
                && Objects.equals(userInfo, that.userInfo)
                && Objects.equals(host, that.host)
                && Objects.equals(pathSegments, that.pathSegments)
                && Objects.equals(queryParams, that.queryParams)
                && Objects.equals(fragment, that.fragment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                ssp, scheme, userInfo, host, port, protocolRelative, rootPath, pathSegments, queryParams, fragment
        );
    }

    @Override
    public String toString() {
        return toUriString();
    }
}
