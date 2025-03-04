package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

class UriBuilder_parseAndRecreateUriSpec extends Specification {
    @Unroll
    def "should parse and recreate uri: #uri"() {
        when:
            UriBuilder result = UriBuilder.fromUri(uri)
        then:
            expectEquals(result, uri)
        where:
            uri << [
                    "file:///~/calendar",
                    "coditory.com",
                    "//coditory.com",
                    "https://coditory.com",
                    "http://coditory.com",
                    "http://john:doe@coditory.com:8080/help?a=a1&a=a2&b=b1#frag",
                    "http://coditory.com?a=a1&a=a2&b=b1#frag",
                    "",
                    "/",
                    "/abc/def",
                    "?a=a1&a=a2&b=b1",
                    "?test=%E8%AA%9E",
                    "?%E8%AA%9E=test",
                    "#%E8%AA%9E",
                    "/%E8%AA%9E",
                    "../../../demo/jfc/SwingSet2/src/SwingSet2.java",
                    "../../../demo/../jfc/SwingSet2/src/SwingSet2.java"
            ]
    }

    @Unroll
    def "should normalize path: #uri"() {
        when:
            UriBuilder result = UriBuilder.fromUri(uri)
        then:
            result.toUriString() == expected
        where:
            uri                           || expected
            "https://coditory.com//test"  || "https://coditory.com/test"
            "https://coditory.com/test/"  || "https://coditory.com/test"
            "https://coditory.com/test//" || "https://coditory.com/test"
    }

    @Unroll
    def "should handle plus sign: #uri"() {
        when:
            UriBuilder result = UriBuilder.fromUri(uri)
        then:
            result.toUriString() == expected
        where:
            uri                                               || expected
            "https://coditory.com/+"                          || "https://coditory.com/+"
            "https://coditory.com?a+b=c+d"                    || "https://coditory.com?a%20b=c%20d"
            "https://coditory.com#a+b"                        || "https://coditory.com#a+b"
            "https://coditory.com/foo+bar?a+b=c+d#x+"         || "https://coditory.com/foo+bar?a%20b=c%20d#x+"
            "https://coditory.com/foo%2Bbar?a%2Bb=c%2Bd#x%2B" || "https://coditory.com/foo+bar?a%2Bb=c%2Bd#x+"
    }

    @Unroll
    def "should parse and recreate opaque uri: #uri"() {
        when:
            UriBuilder result = UriBuilder.fromUri(uri)
        then:
            expectEquals(result, uri)
        where:
            uri << [
                    "mailto:java-net@java.sun.com",
                    "news:comp.lang.java",
                    "urn:isbn:096139210x",
                    "mailto:someone@yoursite.com?subject=Mail%20from%20Our%20Site",
                    "mailto:%E8%AA%9Edef@yoursite.com?subject=Mail%20from%20Our%20Site#frag%E8%AA%9E"
            ]
    }

    private void expectEquals(UriBuilder builder, String expected) {
        String uriString = builder.toUriString()
        assert uriString == expected
        uriString = builder.toUri().toString()
        assert uriString == expected
    }
}
