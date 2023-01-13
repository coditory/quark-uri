package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

class UriBuilderSpec extends Specification {
    def "should build uri from new builder instance"() {
        when:
            String result = new UriBuilder()
                    .setScheme("https")
                    .setUserInfo("john.doe")
                    .setHost("coditory.com")
                    .setPort(8081)
                    .setPath("/test/test2")
                    .addPathSegment("test3")
                    .addQueryParam("a", "a1")
                    .addQueryParam("a", "a2")
                    .addQueryParam("b", "b1")
                    .putQueryParam("b", "b2")
                    .setFragment("x")
                    .buildUriString()
        then:
            result == "https://john.doe@coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2#x"
    }

    def "should normalize path slashes"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com/abc//def/")
                    .addSubPath("//ghi////jkl//")
                    .addPathSegment("mno/pqr")
                    .buildUriString()
        then:
            result == "https://coditory.com/abc/def/ghi/jkl/mno%2Fpqr"
    }

    def "should encode spaces"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com/a+bc//d%20ef/")
                    .addPathSegment("x y ")
                    .setFragment("frag ment")
                    .addQueryParam("f oo", "b ar")
                    .addQueryParam("x", "y+z")
                    .buildUriString()
        then:
            result == "https://coditory.com/a+bc/d%20ef/x%20y%20?f%20oo=b%20ar&x=y%2Bz#frag%20ment"
    }

    def "should replace each uri component"() {
        when:
            String result = UriBuilder.fromUri("https://john.doe@coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2#x")
                    .setScheme("http")
                    .removeUserInfo()
                    .setHost("coditory.xyz")
                    .setDefaultPort()
                    .setPath("/abc")
                    .setQueryParams(w: "W")
                    .setFragment("y")
                    .buildUriString()
        then:
            result == "http://coditory.xyz/abc?w=W#y"
    }

    @Unroll
    def "should build uri without #field"() {
        given:
            UriBuilder builder = UriBuilder.fromUri("https://john.doe@coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2#x")
        when:
            modifier(builder)
        then:
            builder.buildUriString() == uri
        where:
            field               | modifier                                          || uri
            "scheme"            | { UriBuilder b -> b.removeScheme() }              || "john.doe@coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2#x"
            "scheme - relative" | { UriBuilder b -> b.setProtocolRelative(true) }   || "//john.doe@coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2#x"
            "userInfo"          | { UriBuilder b -> b.removeUserInfo() }            || "https://coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2#x"
            "fragment"          | { UriBuilder b -> b.removeFragment() }            || "https://john.doe@coditory.com:8081/test/test2/test3?a=a1&a=a2&b=b2"
            "queryParams"       | { UriBuilder b -> b.removeQueryParams() }         || "https://john.doe@coditory.com:8081/test/test2/test3#x"
            "queryParam-a"      | { UriBuilder b -> b.removeQueryParam("a") }       || "https://john.doe@coditory.com:8081/test/test2/test3?b=b2#x"
            "queryParam-a-a2"   | { UriBuilder b -> b.removeQueryParam("a", "a2") } || "https://john.doe@coditory.com:8081/test/test2/test3?a=a1&b=b2#x"
    }

    @Unroll
    def "should throw error when building uri without host but with scheme and port or userinfo"() {
        when:
            UriBuilder.fromUri("https://john.doe@coditory.com/test/test2/test3?a=a1&a=a2&b=b2#x")
                    .removeHost()
                    .buildUriComponents()
        then:
            InvalidUriException e = thrown(InvalidUriException)
            e.message == "URI with user info must include host"
        when:
            UriBuilder.fromUri("https://coditory.com:8008/test/test2/test3?a=a1&a=a2&b=b2#x")
                    .removeHost()
                    .buildUriComponents()
        then:
            e = thrown(InvalidUriException)
            e.message == "URI with port must include host"
    }

    def "should copy uriBuilder instance"() {
        when:
            UriBuilder first = UriBuilder.fromUri("https://coditory.com/abc")
            UriBuilder second = first.copy().addQueryParam("a", "X")
        then:
            first.buildUriString() == "https://coditory.com/abc"
            second.buildUriString() == "https://coditory.com/abc?a=X"
    }
}
