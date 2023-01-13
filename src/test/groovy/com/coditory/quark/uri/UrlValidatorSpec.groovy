package com.coditory.quark.uri


import spock.lang.Specification
import spock.lang.Unroll

class UrlValidatorSpec extends Specification {
    @Unroll
    def "should pass url validation: #input"() {
        expect:
            UrlValidator.isValidUrl(input) == true
        where:
            input << [
                    "http://coditory.com",
                    "http://coditory.com/abc",
                    "http://coditory.com/a%20b",
                    "http://coditory.com/a%E8%AA%9Eb",
                    "http://coditory.com/a/b/c",
                    "http://coditory.com/test?a=b&a=b&a=c"
            ]
    }

    @Unroll
    def "should fail url validation: #input"() {
        expect:
            UrlValidator.isValidUrl(input) == false
        where:
            input << [
                    // invalid scheme
                    "htttp://coditory.com",
                    "htp://coditory.com",
                    "://coditory.com",
                    "1ht://coditory.com",
                    "coditory.com",
                    // invalid host
                    "http://co+ditory.com",
                    "http://coditory.1co",
                    "http://coditory.",
                    "http://.com",
                    "http://",
                    // invalid port
                    "http://coditory.com:-5",
                    "http://coditory.com:65636",
                    "http://coditory.com:80a",
                    "http://coditory.com:a80",
                    // invalid path
                    "http://coditory.com/..",
                    "http://coditory.com/../",
                    "http://coditory.com/../file",
                    "http://coditory.com/abc/../../file",
            ]
    }
}
