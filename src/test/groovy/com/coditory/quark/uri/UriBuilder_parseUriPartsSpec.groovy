package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

class UriBuilder_parseUriPartsSpec extends Specification {
    @Unroll
    def "should parse scheme and host: #input"() {
        when:
            UriComponents result = UriComponents.fromUri(input)
        then:
            result.scheme == expected
        and:
            result.host == "coditory.com"
        where:
            input                    || expected
            "//coditory.com"         || null
            "https://coditory.com"   || "https"
            "http://coditory.com"    || "http"
            "unknown://coditory.com" || "unknown"
    }

    def "should handle plus sign"() {
        when:
            UriComponents result = UriComponents.fromUri("https://coditory.com/foo+bar?a+b=c+d#x+y")
        then:
            result.pathSegments == ["foo+bar"]
            result.queryMultiParams == ["a b": ["c d"]]
            result.fragment == "x+y"
    }

    def "should parse protocol relative uri"() {
        when:
            UriComponents result = UriComponents.fromUri("//coditory.com")
        then:
            result.protocolRelative == true
            result.scheme == null
            result.isOpaque() == false
            result.schemeSpecificPart == null
    }

    @Unroll
    def "should parse user info: #input"() {
        when:
            UriComponents result = UriComponents.fromUri(input)
        then:
            result.userInfo == expected
        where:
            input                               || expected
            "https://john@coditory.com"         || "john"
            "https://user:pass123@coditory.com" || "user:pass123"
    }

    @Unroll
    def "should parse path segments: #input"() {
        when:
            UriComponents result = UriComponents.fromUri(input)
        then:
            result.pathSegments == expected
        and:
            result.rootPath == rootPath
        where:
            input                                        || rootPath | expected
            "//coditory.com"                             || true     | []
            "https://coditory.com//"                     || true     | []
            "https://////coditory.com/////"              || true     | ["coditory.com"]
            "http://coditory.com///test/////test2//////" || true     | ["test", "test2"]
            "/test"                                      || true     | ["test"]
            "../test"                                    || false    | ["..", "test"]
            "test"                                       || false    | ["test"]
            "test//test2///"                             || false    | ["test", "test2"]
            "/a+b/c%20d"                                 || true     | ["a+b", "c d"]
            "/%F0%9D%84%9E/%E8%AA%9E/a"                  || true     | ["𝄞", "語", "a"]
    }

    @Unroll
    def "should parse query params: #query"() {
        when:
            UriComponents result = UriComponents.fromUri("https://coditory.com" + query)
        then:
            result.queryMultiParams == expected

        and:
            result.queryParams == expectedSingleValue

        where:
            query          | expected             | expectedSingleValue
            ""             | [:]                  | [:]
            "?"            | [:]                  | [:]
            "?a"           | [a: [""]]            | [a: ""]
            "?a="          | [a: [""]]            | [a: ""]
            "?a=&a="       | [a: ["", ""]]        | [a: ""]
            "?a=b"         | [a: ["b"]]           | [a: "b"]
            "?a=b&a=c"     | [a: ["b", "c"]]      | [a: "b"]
            "?a=b&a=b"     | [a: ["b", "b"]]      | [a: "b"]
            "?a=b&c=d"     | [a: ["b"], c: ["d"]] | [a: "b", c: "d"]
            "?a+b=c+d"     | ["a b": ["c d"]]     | ["a b": "c d"]
            "?a%20b=c%20d" | ["a b": ["c d"]]     | ["a b": "c d"]
            "?a=%E8%AA%9E" | [a: ["語"]]          | [a: "語"]
    }

    @Unroll
    def "should parse fragment: #fragment"() {
        when:
            UriComponents result = UriComponents.fromUri("https://coditory.com" + fragment)
        then:
            result.fragment == expected

        where:
            fragment       | expected
            ""             | null
            "#"            | null
            "#a"           | "a"
            "#a%20b"       | "a b"
            "#a%E8%AA%9Eb" | "a語b"
    }

    @Unroll
    def "should throw error when parsing invalid uri string: #uri"() {
        when:
            UriBuilder.fromUri(uri)
        then:
            InvalidUriException e = thrown(InvalidUriException)
            e.message == "Could not parse uri: \"$uri\". Cause: $message"
        where:
            uri                              | message
            "https://john.doe@"              | "URI with user info must include host"
            "https://john.doe@:8080"         | "URI with user info must include host"
            "https://coditory.com:80a"       | "Invalid character 'a' for port in \"80a\""
            "https://coditory.com:a80"       | "Invalid character 'a' for port in \"a80\""
            "https://:8080"                  | "URI with port must include host"
            "https://codi  tory.com"         | "Invalid character ' ' for host in \"codi  tory.com\""
            "ht tps://coditory.com"          | "Invalid character ' ' for scheme in \"ht tps\""
            "https://jo hn.doe@coritory.com" | "Invalid character ' ' for user_info in \"jo hn.doe\""
            "/ /"                            | "Invalid character ' ' for path_segment in \" \""
            "/test?a b=c"                    | "Invalid character ' ' for query in \"a b=c\""
            "/test?a=b c"                    | "Invalid character ' ' for query in \"a=b c\""
            "/test?a=b=c"                    | "Invalid character '=' for query_param in \"b=c\""
            "/test#a b"                      | "Invalid character ' ' for fragment in \"a b\""
            "/test#a#b"                      | "Invalid character '#' for fragment in \"a#b\""
    }
}
