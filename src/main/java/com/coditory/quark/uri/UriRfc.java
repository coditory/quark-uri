package com.coditory.quark.uri;

import java.util.BitSet;

import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static java.util.Locale.ROOT;

enum UriRfc {
    SCHEME(UriRfcCharacters.SCHEME_ALLOWED),
    SCHEME_SPECIFIC_PART(UriRfcCharacters.SCHEME_SPECIFIC_PART_ALLOWED),
    USER_INFO(UriRfcCharacters.USER_INFO_ALLOWED),
    HOST(UriRfcCharacters.HOST_IPV6_ALLOWED),
    PORT(UriRfcCharacters.PORT_ALLOWED),
    PATH_SEGMENT(UriRfcCharacters.PATH_SEGMENT_ALLOWED),
    QUERY(UriRfcCharacters.QUERY_ALLOWED, true),
    QUERY_PARAM(UriRfcCharacters.QUERY_PARAM_ALLOWED, true),
    QUERY_PARAM_NARROW(UriRfcCharacters.QUERY_PARAM_ALLOWED_NARROW, true),
    FRAGMENT(UriRfcCharacters.FRAGMENT_ALLOWED);

    private final BitSet allowed;
    private final PercentCodec codec;

    UriRfc(String allowed) {
        this(allowed, false);
    }

    UriRfc(String allowed, boolean decodeSpaceAsPlus) {
        this.allowed = BitSets.of(allowed);
        String encode = decodeSpaceAsPlus
                ? allowed.replaceAll("\\+", "")
                : allowed;
        this.codec = PercentCodec.builder()
                .safeCharacters(encode)
                .decodeSpaceAsPlus(decodeSpaceAsPlus)
                .build();
    }

    String validateAndDecode(String source) {
        checkValidEncoded(source);
        return decode(source);
    }

    String decode(String source) {
        StringBuilder builder = new StringBuilder();
        decode(source, builder);
        return builder.toString();
    }

    void decode(String source, StringBuilder builder) {
        codec.decode(source, builder);
    }

    String encode(String source) {
        StringBuilder builder = new StringBuilder();
        encode(source, builder);
        return builder.toString();
    }

    void encode(String source, StringBuilder builder) {
        codec.encode(source, builder);
    }

    void checkValidEncoded(String source) {
        String error = checkValidEncodedWithErrorMessage(source);
        if (error != null) {
            throw new InvalidUriException(error);
        }
    }

    private String checkValidEncodedWithErrorMessage(String source) {
        expectNonNull(source, "source");
        int length = source.length();
        for (int i = 0; i < length; i++) {
            char ch = source.charAt(i);
            if (ch == '%') {
                if ((i + 2) < length) {
                    char hex1 = source.charAt(i + 1);
                    char hex2 = source.charAt(i + 2);
                    int u = Character.digit(hex1, 16);
                    int l = Character.digit(hex2, 16);
                    if (u == -1 || l == -1) {
                        return "Invalid encoded sequence \"" + source.substring(i) + "\"";
                    }
                    i += 2;
                } else {
                    return "Invalid encoded sequence \"" + source.substring(i) + "\"";
                }
            } else if (!allowed.get(ch)) {
                return "Invalid character '" + ch + "' for " + name().toLowerCase(ROOT) + " in \"" + source + "\"";
            }
        }
        return null;
    }

}
