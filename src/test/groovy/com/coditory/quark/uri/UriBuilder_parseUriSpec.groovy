package com.coditory.quark.uri

import spock.lang.Specification

class UriBuilder_parseUriSpec extends Specification {
    def "should parse UriComponents from complex http url"() {
        when:
            UriComponents result = UriComponents.fromUri("https://john:doe@coditory.com:8080/help/test?a=a1&a=a2&b=b1#frag")
        then:
            result.schemeSpecificPart == null
            result.opaque == false
            result.scheme == "https"
            result.userInfo == "john:doe"
            result.uriAuthority == UriAuthority.of("john:doe", "coditory.com", 8080)
            result.host == "coditory.com"
            result.port == 8080
            result.path == "/help/test"
            result.pathSegments == ["help", "test"]
            result.protocolRelative == false
            result.rootPath == true
            result.queryString == "a=a1&a=a2&b=b1"
            result.queryMultiParams == [
                    a: ['a1', 'a2'],
                    b: ['b1']
            ]
            result.fragment == "frag"
            result.isHttpUrl() == true
    }

    def "should parse UriComponents from basic http url"() {
        when:
            UriComponents result = UriBuilder
                    .fromUri("http://coditory.com/help/test?a=a1&a=a2&b=b1#frag")
                    .toUriComponents()
        then:
            result.schemeSpecificPart == null
            result.opaque == false
            result.scheme == "http"
            result.userInfo == null
            result.uriAuthority == UriAuthority.of(null, "coditory.com", -1)
            result.host == "coditory.com"
            result.port == -1
            result.path == "/help/test"
            result.pathSegments == ["help", "test"]
            result.rootPath == true
            result.queryString == "a=a1&a=a2&b=b1"
            result.queryMultiParams == [
                    a: ['a1', 'a2'],
                    b: ['b1']
            ]
            result.fragment == "frag"
            result.isHttpUrl() == true
    }

    def "should parse UriComponents from uri starting with path"() {
        when:
            UriComponents result = UriBuilder
                    .fromUri("/help/test?a=a1&a=a2&b=b1#frag")
                    .toUriComponents()
        then:
            result.schemeSpecificPart == null
            result.opaque == false
            result.scheme == null
            result.userInfo == null
            result.uriAuthority == null
            result.host == null
            result.port == -1
            result.path == "/help/test"
            result.pathSegments == ["help", "test"]
            result.rootPath == true
            result.queryString == "a=a1&a=a2&b=b1"
            result.queryMultiParams == [
                    a: ['a1', 'a2'],
                    b: ['b1']
            ]
            result.fragment == "frag"
            result.isHttpUrl() == false
    }

    def "should parse UriComponents from uri with scheme and host only"() {
        when:
            UriComponents result = UriBuilder
                    .fromUri("https://coditory.com")
                    .toUriComponents()
        then:
            result.schemeSpecificPart == null
            result.opaque == false
            result.scheme == "https"
            result.userInfo == null
            result.uriAuthority == UriAuthority.of(null, "coditory.com", -1)
            result.host == "coditory.com"
            result.port == -1
            result.path == null
            result.pathSegments == []
            result.rootPath == true
            result.queryString == null
            result.queryMultiParams == [:]
            result.fragment == null
            result.isHttpUrl() == true
    }

    def "should parse UriComponents from opaque uri"() {
        when:
            UriComponents result = UriBuilder
                    .fromUri("mailto:%E8%AA%9Edef@yoursite.com?subject=Mail%20from%20Our%20Site#frag%E8%AA%9E")
                    .toUriComponents()
        then:
            result.schemeSpecificPart == "語def@yoursite.com?subject=Mail from Our Site"
            result.opaque == true
            result.scheme == "mailto"
            result.userInfo == null
            result.uriAuthority == null
            result.host == null
            result.port == -1
            result.path == null
            result.pathSegments == []
            result.rootPath == false
            result.queryString == null
            result.queryMultiParams == [:]
            result.fragment == "frag語"
            result.isHttpUrl() == false
    }
}
