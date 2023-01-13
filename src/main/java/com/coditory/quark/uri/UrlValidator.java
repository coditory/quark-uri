package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coditory.quark.uri.InetAddressValidator.isValidInetV4Address;
import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static com.coditory.quark.uri.Strings.isNullOrEmpty;

public final class UrlValidator {
    private static final UrlValidator INSTANCE = builder().build();

    public static UrlValidator instance() {
        return INSTANCE;
    }

    public static boolean isValidUrl(String url) {
        return INSTANCE.isValid(url);
    }

    public static boolean isValidUrl(UriComponents uriComponents) {
        return INSTANCE.isValid(uriComponents);
    }

    private static final String ALPHA_CHARS = "a-zA-Z";
    private static final String SPECIAL_CHARS = ";/@&=,.?:+$";
    private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";
    private static final String ATOM = VALID_CHARS + '+';
    private static final Pattern SCHEME_PATTERN = Pattern.compile("^\\p{Alpha}[\\p{Alnum}\\+\\-\\.]*");
    private static final Pattern PATH_PATTERN = Pattern.compile("^(/[-\\w:@&?=+,.!/~*'%$_;]*)?$");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^" + ATOM + "(\\." + ATOM + ")*$");
    private static final Pattern ATOM_PATTERN = Pattern.compile("^(" + ATOM + ").*?$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[" + ALPHA_CHARS + "]");
    private final boolean allowFragments;
    private final Set<String> allowedSchemes;

    UrlValidator(boolean allowFragments, Set<String> allowedSchemes) {
        this.allowFragments = allowFragments;
        this.allowedSchemes = Set.copyOf(allowedSchemes);
    }

    public boolean isValid(String url) {
        expectNonNull(url, "url");
        UriComponents uriComponents;
        try {
            uriComponents = UriComponents.fromHttpUrl(url);
        } catch (Exception e) {
            return false;
        }
        return isValid(uriComponents);
    }

    public boolean isValid(UriComponents uriComponents) {
        expectNonNull(uriComponents, "uriComponents");
        if (!isValidScheme(uriComponents.getScheme())) {
            return false;
        }
        if (!isValidHost(uriComponents.getHost())) {
            return false;
        }
        if (!isValidPath(uriComponents.getPath())) {
            return false;
        }
        if (!isValidFragment(uriComponents.getFragment())) {
            return false;
        }
        return true;
    }

    boolean isValidScheme(@Nullable String scheme) {
        if (scheme == null) {
            return false;
        }
        if (!SCHEME_PATTERN.matcher(scheme).matches()) {
            return false;
        }
        return allowedSchemes == null || allowedSchemes.contains(scheme);
    }

    boolean isValidHost(@Nullable String host) {
        if (isNullOrEmpty(host)) {
            return false;
        }
        boolean hostname = false;
        boolean ipV4Address = isValidInetV4Address(host);
        if (!ipV4Address) {
            hostname = DOMAIN_PATTERN.matcher(host).matches();
        }
        if (hostname) {
            char[] chars = host.toCharArray();
            int size = 1;
            for (char element : chars) {
                if (element == '.') {
                    size++;
                }
            }
            String[] domainSegment = new String[size];
            boolean match = true;
            int segmentCount = 0;
            int segmentLength = 0;
            while (match) {
                Matcher atomMatcher = ATOM_PATTERN.matcher(host);
                match = atomMatcher.matches();
                if (match) {
                    domainSegment[segmentCount] = atomMatcher.group(1);
                    segmentLength = domainSegment[segmentCount].length() + 1;
                    host =
                            (segmentLength >= host.length())
                                    ? ""
                                    : host.substring(segmentLength);

                    segmentCount++;
                }
            }
            String topLevel = domainSegment[segmentCount - 1];
            if (topLevel.length() < 2 || topLevel.length() > 4) {
                return false;
            }
            if (!ALPHA_PATTERN.matcher(topLevel.substring(0, 1)).matches()) {
                return false;
            }
            if (segmentCount < 2) {
                return false;
            }
        }
        return hostname || ipV4Address;
    }

    boolean isValidPath(@Nullable String path) {
        if (isNullOrEmpty(path)) {
            return true;
        }
        if (!PATH_PATTERN.matcher(path).matches()) {
            return false;
        }
        int slashCount = countToken("/", path);
        int dot2Count = countToken("..", path);
        return dot2Count <= 0 || (slashCount - 2) > dot2Count;
    }

    boolean isValidFragment(@Nullable String fragment) {
        return fragment == null || allowFragments;
    }

    private int countToken(String token, String target) {
        int tokenIndex = 0;
        int count = 0;
        while (tokenIndex != -1) {
            tokenIndex = target.indexOf(token, tokenIndex);
            if (tokenIndex > -1) {
                tokenIndex++;
                count++;
            }
        }
        return count;
    }

    @NotNull
    public static UrlValidatorBuilder builder() {
        return new UrlValidatorBuilder();
    }

    public static class UrlValidatorBuilder {
        private boolean allowFragments = true;
        private Set<String> allowedSchemes = Set.of("http", "https");

        public UrlValidatorBuilder allowFragments(boolean allowFragments) {
            this.allowFragments = allowFragments;
            return this;
        }

        public UrlValidatorBuilder allowedSchemes(Set<String> allowedSchemes) {
            this.allowedSchemes = expectNonNull(allowedSchemes, "allowedSchemes");
            return this;
        }

        public UrlValidator build() {
            return new UrlValidator(allowFragments, allowedSchemes);
        }
    }
}
