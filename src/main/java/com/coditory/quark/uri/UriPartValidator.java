package com.coditory.quark.uri;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coditory.quark.uri.InetAddressValidator.isValidInetV4Address;

final class UriPartValidator {
    private static final String SPECIAL_CHARS = ";/@&=,.?:+$";
    private static final String VALID_CHARS = "[^\\s" + SPECIAL_CHARS + "]";
    private static final String ATOM = VALID_CHARS + '+';
    private static final Pattern SCHEME_PATTERN = Pattern.compile("^\\p{Alpha}[\\p{Alnum}\\+\\-\\.]*");
    private static final Pattern ATOM_PATTERN = Pattern.compile("^(" + ATOM + ").*?$");
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^" + ATOM + "(\\." + ATOM + ")*$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[a-zA-Z]");

    static boolean isValidScheme(String scheme) {
        return scheme != null && SCHEME_PATTERN.matcher(scheme).matches();
    }

    static void checkScheme(String scheme) {
        if (!isValidScheme(scheme)) {
            throw new InvalidUriException("Invalid scheme: " + scheme);
        }
    }

    static boolean isValidHost(String host) {
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
                    host = segmentLength >= host.length()
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

    static void checkHost(String host) {
        if (!isValidHost(host)) {
            throw new InvalidUriException("Invalid host: " + host);
        }
    }

    static boolean isValidPort(int port) {
        return Ports.isValidPortNumber(port);
    }

    static void checkPort(int port) {
        if (!isValidPort(port)) {
            throw new InvalidUriException("Invalid port: " + port);
        }
    }
}
