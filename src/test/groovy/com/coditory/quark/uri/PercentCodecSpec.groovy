package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

import static com.coditory.quark.uri.base.Alphabets.ASCII_CONTROL_CODES
import static com.coditory.quark.uri.base.Alphabets.ASCII_PRINTABLE
import static com.coditory.quark.uri.base.Alphabets.URI_UNRESERVED

class PercentCodecSpec extends Specification {
    @Unroll
    def "should encode and decode string: #input"() {
        when:
            String encoded = PercentCodec.encodeUriComponent(input)
        then:
            encoded == expected

        when:
            String decoded = PercentCodec.decodeUriComponent(encoded)
        then:
            decoded == input

        where:
            input               || expected
            ""                  || ""
            "  \t\n+"           || "%20%20%09%0A%2B"
            "a𝄞b"               || "a%F0%9D%84%9Eb"
            "語"                || "%E8%AA%9E"
            "®"                 || "%C2%AE"
            "🌉"                 || "%F0%9F%8C%89"
            "語®🌉"              || "%E8%AA%9E%C2%AE%F0%9F%8C%89"
            URI_UNRESERVED      || "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
            ASCII_PRINTABLE     || "%20%21%22%23%24%25%26%27%28%29%2A%2B%2C-.%2F0123456789%3A%3B%3C%3D%3E%3F%40ABCDEFGHIJKLMNOPQRSTUVWXYZ%5B%5C%5D%5E_%60abcdefghijklmnopqrstuvwxyz%7B%7C%7D~"
            ASCII_CONTROL_CODES || "%00%01%02%03%04%05%06%07%08%09%0A%0B%0C%0D%0E%0F%10%11%12%13%14%15%16%17%18%19%1A%1B%1C%1D%1E%1F%7F"
    }

    def "should encode space with plus when configured"() {
        given:
            PercentCodec codec = PercentCodec.builder()
                    .spaceAsPlus(true)
                    .build()
        when:
            String encoded = codec.encode("  \t\n")
        then:
            encoded == "++%09%0A"

        when:
            String decoded = codec.decode(encoded)
        then:
            decoded == "  \t\n"
    }

    def "should use configured safe characters"() {
        given:
            PercentCodec codec = PercentCodec.builder()
                    .safeCharacters("abc語")
                    .build()
        when:
            String encoded = codec.encode("ABCabc®語")
        then:
            encoded == "%41%42%43abc%C2%AE語"

        when:
            String decoded = codec.decode(encoded)
        then:
            decoded == "ABCabc®語"
    }
}
