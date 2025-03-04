package com.coditory.quark.uri

import spock.lang.Specification

class UriBuilder_queryParamsSpec extends Specification {
    def "should decode query param with url encoded new and old way"() {
        when:
            String redirect = UriComponents.fromUri("https://coditory.com?redirect=http://localhost:8080/abc?a%3Db%26c%3Dd")
                    .getQueryParam("redirect")
        then:
            redirect == "http://localhost:8080/abc?a=b&c=d"

        and:
            String redirect2 = UriComponents.fromUri("https://coditory.com?redirect=http%3A%2F%2Flocalhost%3A8080%2Fabc%3Fa%3Db%26c%3Dd")
                    .getQueryParam("redirect")
        then:
            redirect2 == "http://localhost:8080/abc?a=b&c=d"
    }

    def "should encode query param with url the old way"() {
        when:
            String uri = UriBuilder.fromUri("https://coditory.com")
                    .addQueryParam("redirect", "http://localhost:8080/abc?a=b&c=d")
                    .toUriString()
        then:
            uri == "https://coditory.com?redirect=http%3A%2F%2Flocalhost%3A8080%2Fabc%3Fa%3Db%26c%3Dd"
    }

    def "should parse query param"() {
        when:
            UriBuilder result1 = UriBuilder.fromQueryString("?a=B&c=D")
            UriBuilder result2 = UriBuilder.fromQueryString("a=B&c=D")
        then:
            result1.toUriString() == "?a=B&c=D"
            result1.toUriString() == result2.toUriString()
    }

    def "should sort query params"() {
        when:
            String result = UriBuilder.fromQueryString("?a=C&c=A2&c=A1&b=B")
                    .sortQueryParams()
                    .toUriString()
        then:
            result == "?a=C&b=B&c=A2&c=A1"
    }

    def "should sort query param values"() {
        when:
            String result = UriBuilder.fromQueryString("?a=C&c=A2&c=A1&b=B")
                    .sortQueryParamValues()
                    .toUriString()
        then:
            result == "?a=C&c=A1&c=A2&b=B"
    }


    def "should sort query params and values"() {
        when:
            String result = UriBuilder.fromQueryString("?a=C&c=A2&c=A1&b=B")
                    .sortQueryParamsAndValues()
                    .toUriString()
        then:
            result == "?a=C&b=B&c=A1&c=A2"
    }

    def "should add query params with addQueryParam"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .addQueryParam("a", "X")
                    .addQueryParam("a", "X")
                    .addQueryParam("a", "Y")
                    .addQueryParam("b", "Y")
                    .addQueryParam("e", "")
                    .toUriString()
        then:
            result == "https://coditory.com?w=W&a=A&a=X&a=X&a=Y&b=Y&e="
    }

    def "should add query params with addQueryMultiParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .addQueryMultiParams([
                            a : ["X", "X", "Y"],
                            b : ["Y"],
                            e1: [],
                            e2: [""]
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?w=W&a=A&a=X&a=X&a=Y&b=Y&e2="
    }

    def "should add query params with addQueryParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .addQueryParams([
                            a: "X",
                            b: "Y",
                            e: ""
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?w=W&a=A&a=X&b=Y&e="
    }

    def "should add query params with putQueryParam"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .putQueryParam("a", "X")
                    .putQueryParam("a", "X")
                    .putQueryParam("a", "Y")
                    .putQueryParam("b", "Y")
                    .putQueryParam("e", "")
                    .toUriString()
        then:
            result == "https://coditory.com?w=W&a=Y&b=Y&e="
    }

    def "should add query params with putQueryMultiParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .putQueryMultiParams([
                            a : ["X", "X", "Y"],
                            b : ["Y"],
                            e1: [],
                            e2: [""]
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?w=W&a=X&a=X&a=Y&b=Y&e2="
    }

    def "should add query params with putQueryParams"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?w=W&a=A")
                    .putQueryParams([
                            a: "X",
                            b: "Y",
                            e: ""
                    ])
                    .toUriString()
        then:
            result == "https://coditory.com?w=W&a=X&b=Y&e="
    }

    def "should remove query param by name"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?a=X&a=Y&b=Z")
                    .removeQueryParam("a")
                    .toUriString()
        then:
            result == "https://coditory.com?b=Z"
    }

    def "should remove query param by name and value"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?a=X&a=Y&b=Z")
                    .removeQueryParam("a", "Y")
                    .toUriString()
        then:
            result == "https://coditory.com?a=X&b=Z"
    }

    def "should remove all query params"() {
        when:
            String result = UriBuilder.fromUri("https://coditory.com?a=X&a=Y&b=Z")
                    .removeQueryParams()
                    .toUriString()
        then:
            result == "https://coditory.com"
    }
}
