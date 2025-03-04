package com.coditory.quark.uri;

final class UriRfcCharacters {
    private UriRfcCharacters() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    private static final String ALPHABETIC_UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHABETIC_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String ALPHABETIC = ALPHABETIC_UPPER + ALPHABETIC_LOWER;
    private static final String NUMERIC = "0123456789";
    private static final String ALPHANUMERIC = ALPHABETIC + NUMERIC;
    static final String URI_DELIMITER = ":/?#[]@";
    static final String URI_SUB_DELIMITER = "!$&'()*+,;=";
    static final String URI_RESERVED = URI_DELIMITER + URI_SUB_DELIMITER;
    static final String URI_UNRESERVED = ALPHABETIC + NUMERIC + "-._~";
    static final String URI_PCHAR = ":@" + URI_UNRESERVED + URI_SUB_DELIMITER;

    static final String SCHEME_ALLOWED = ALPHANUMERIC + "+-.";
    static final String SCHEME_SPECIFIC_PART_ALLOWED = SCHEME_ALLOWED + URI_PCHAR + URI_RESERVED;
    static final String USER_INFO_ALLOWED = URI_UNRESERVED + URI_SUB_DELIMITER + ":";
    static final String HOST_IPV6_ALLOWED = URI_UNRESERVED + URI_SUB_DELIMITER + "[]:";
    static final String PORT_ALLOWED = NUMERIC;
    static final String PATH_SEGMENT_ALLOWED = URI_PCHAR;
    static final String QUERY_ALLOWED = URI_PCHAR + "/?";
    static final String QUERY_PARAM_ALLOWED = QUERY_ALLOWED
            .replaceFirst("=", "").replaceFirst("&", "");
    static final String QUERY_PARAM_ALLOWED_NARROW = QUERY_PARAM_ALLOWED
            // Deliberately encode '?' and '/' even though it's not required.
            // In practice, it's a less problematic and questionable
            // https://datatracker.ietf.org/doc/html/rfc3986#section-3.4
            .replaceFirst("\\?", "").replaceFirst("/", "").replaceFirst(":", "");
    static final String FRAGMENT_ALLOWED = URI_PCHAR + "/?";
}
