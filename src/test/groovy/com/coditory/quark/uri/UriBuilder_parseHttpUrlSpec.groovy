package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

class UriBuilder_parseHttpUrlSpec extends Specification {
    @Unroll
    def "should create URL from valid http url: #input"() {
        when:
            UriComponents result = UriBuilder.fromUri(input).buildUriComponents()
        then:
            result.isValidHttpUrl() == true
        and:
            result.toUrl() == new URL(input)
        where:
            input << [
                    "https://coditory.com",
                    "http://coditory.com",
                    "http://john:doe@coditory.com:8080/help?a=a1&a=a2&b=b1#frag",
                    "http://coditory.com?a=a1&a=a2&b=b1#frag"
            ]
    }

    @Unroll
    def "should detect invalid http url: #input"() {
        when:
            UriComponents result = UriBuilder.fromUri(input).buildUriComponents()
        then:
            result.isValidHttpUrl() == false
        where:
            input << [
                    "ftp://coditory.com",
                    "htttps://coditory.com",
                    "coditory.com",
                    "",
                    "/",
                    "/abc/def",
                    "?a=a1&a=a2&b=b1",
                    "?test=%E8%AA%9E",
                    "?%E8%AA%9E=test",
                    "#%E8%AA%9E",
                    "/%E8%AA%9E",
                    "../../../demo/jfc/SwingSet2/src/SwingSet2.java",
                    "mailto:java-net@java.sun.com",
                    "news:comp.lang.java",
                    "urn:isbn:096139210x"
            ]
    }
}
