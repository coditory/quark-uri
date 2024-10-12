package com.coditory.quark.uri

import spock.lang.Specification

class HostsSpec extends Specification {
    def "should retrieve non blank localHostName"() {
        when:
            String localHostName = Hosts.localHostName
        then:
            !localHostName.isBlank()
    }

    def "should retrieve non blank localHostAddress"() {
        when:
            String localHostAddress = Hosts.localHostAddress
        then:
            !localHostAddress.isBlank()
    }
}
