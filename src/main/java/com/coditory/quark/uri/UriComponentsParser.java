package com.coditory.quark.uri;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coditory.quark.uri.Nullable.onNotNull;
import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static com.coditory.quark.uri.Strings.isNotNullOrEmpty;

class UriComponentsParser {
    private static final Pattern QUERY_PARAM_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");
    private static final String SCHEME_PATTERN = "([^:/?#]+):";
    private static final String USERINFO_PATTERN = "([^@\\[/?#]*)";
    private static final String HOST_IPV4_PATTERN = "[^\\[/?#:]*";
    private static final String HOST_IPV6_PATTERN = "\\[[\\p{XDigit}:.]*[%\\p{Alnum}]*]";
    private static final String HOST_PATTERN = "(" + HOST_IPV6_PATTERN + "|" + HOST_IPV4_PATTERN + ")";
    private static final String PORT_PATTERN = "([^/?#]*)";
    private static final String PATH_PATTERN = "([^?#]*)";
    private static final String QUERY_PATTERN = "([^#]*)";
    private static final String LAST_PATTERN = "(.*)";

    // Regex patterns that matches URIs. See RFC 3986, appendix B
    private static final Pattern URI_PATTERN = Pattern.compile(
            "^(" + SCHEME_PATTERN + ")?" +
                    "(//(" + USERINFO_PATTERN + "@)?" + HOST_PATTERN + "(:" + PORT_PATTERN + ")?" + ")?" +
                    PATH_PATTERN + "(\\?" + QUERY_PATTERN + ")?" + "(#" + LAST_PATTERN + ")?");

    static UriBuilder parseUrl(String uri) {
        expectNonNull(uri, "uri");
        try {
            UriBuilder builder = UriComponentsParser.parseUri(uri);
            UriComponents components = builder.toUriComponents();
            if (!components.isHttpUrl()) {
                throw new InvalidHttpUrlException("Invalid http url: \"" + uri + "\"");
            }
            return builder;
        } catch (InvalidUriException e) {
            InvalidUriException root = Throwables.getRootCauseOfType(e, InvalidUriException.class);
            String suffix = root == null ? "" : ". Cause: " + root.getMessage();
            throw new InvalidHttpUrlException("Could not parse http url: \"" + uri + "\"" + suffix, root);
        } catch (RuntimeException e) {
            throw new InvalidHttpUrlException("Could not parse http url: \"" + uri + "\"", e);
        }
    }

    static UriBuilder parseUrlOrNull(String uri) {
        expectNonNull(uri, "uri");
        UriBuilder builder = UriComponentsParser.parseUriOrNull(uri);
        if (builder == null) {
            return null;
        }
        UriComponents components = builder.toUriComponents();
        if (!components.isHttpUrl()) {
            return null;
        }
        return builder;
    }

    static UriBuilder parseUri(String uri) {
        expectNonNull(uri, "uri");
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            throw new InvalidUriException("Could not parse uri: \"" + uri + "\"");
        }
        try {
            return parseUri(uri, matcher);
        } catch (InvalidUriException e) {
            throw new InvalidUriException("Could not parse uri: \"" + uri + "\". Cause: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new InvalidUriException("Could not parse uri: \"" + uri + "\"");
        }
    }

    static UriBuilder parseUriOrNull(String uri) {
        expectNonNull(uri, "uri");
        Matcher matcher = URI_PATTERN.matcher(uri);
        if (!matcher.matches()) {
            return null;
        }
        try {
            return parseUri(uri, matcher);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static UriBuilder parseUri(String uri, Matcher matcher) {
        UriBuilder builder = new UriBuilder();
        String scheme = matcher.group(2);
        String userInfo = matcher.group(5);
        String host = matcher.group(6);
        String port = matcher.group(8);
        String path = matcher.group(9);
        String query = matcher.group(11);
        String fragment = matcher.group(13);
        boolean opaque = false;
        if (isNotNullOrEmpty(scheme)) {
            String rest = uri.substring(scheme.length());
            if (!rest.startsWith(":/")) {
                opaque = true;
            }
            builder.setScheme(UriRfc.SCHEME.validateAndDecode(scheme));
        } else if (uri.startsWith("//")) {
            builder.setProtocolRelative(true);
        }
        if (opaque) {
            String ssp = uri.substring(scheme.length()).substring(1);
            if (isNotNullOrEmpty(fragment)) {
                ssp = ssp.substring(0, ssp.length() - (fragment.length() + 1));
            }
            builder.setSchemeSpecificPart(UriRfc.SCHEME_SPECIFIC_PART.validateAndDecode(ssp));
        } else {
            onNotNull(userInfo, it -> builder.setUserInfo(UriRfc.USER_INFO.validateAndDecode(it)));
            onNotNull(host, it -> builder.setHost(UriRfc.HOST.validateAndDecode(it)));
            onNotNull(port, it -> {
                String decodedPort = UriRfc.PORT.validateAndDecode(it);
                builder.setPort(Integer.parseInt(decodedPort));
            });
            onNotNull(path, builder::setPath);
            onNotNull(query, q -> builder.setQueryMultiParams(parseQuery(q)));
        }
        onNotNull(fragment, it -> builder.setFragment(UriRfc.FRAGMENT.validateAndDecode(it)));
        builder.validate();
        return builder;
    }

    static Map<String, List<String>> parseQuery(String query) {
        expectNonNull(query, "query");
        query = query.startsWith("?") ? query.substring(1) : query;
        Matcher matcher = QUERY_PARAM_PATTERN.matcher(query);
        UriRfc.QUERY.checkValidEncoded(query);
        Map<String, List<String>> result = new LinkedHashMap<>();
        while (matcher.find()) {
            String name = matcher.group(1);
            String decodedName = UriRfc.QUERY_PARAM.validateAndDecode(name);
            String value = matcher.group(3);
            String normalizedValue = value != null ? value : "";
            result
                    .computeIfAbsent(decodedName, k -> new ArrayList<>())
                    .add(UriRfc.QUERY_PARAM.validateAndDecode(normalizedValue));
        }
        return result;
    }
}
