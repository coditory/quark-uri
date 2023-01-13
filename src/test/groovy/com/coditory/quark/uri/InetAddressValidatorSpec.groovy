package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

class InetAddressValidatorSpec extends Specification {
    private static final List<String> validIpV4 = [
            "127.0.0.1",
            "8.8.8.8"
    ]
    private static final List<String> invalidIpV4 = [
            "127.0.0.1.1",
            "127.0.1",
            "127.0..1",
            "127.888.0.1",
    ]
    private static final List<String> validIpV6 = [
            "2001:db8:3333:4444:5555:6666:7777:8888",
            "2001:db8::1234:5678",
            "::1234:5678"
    ]
    private static final List<String> invalidIpV6 = [
            ":::1234:5678",
            "2001:db8:3333:4444:5555:6666:7777:8888:8888",
    ]

    @Unroll
    def "should validate valid inet address: #address"() {
        when:
            InetAddressValidator.validateInetAddress(address)
        then:
            noExceptionThrown()
        where:
            address << validIpV4 + validIpV6
    }

    @Unroll
    def "should validate invalid inet address: #address"() {
        when:
            InetAddressValidator.validateInetAddress(address)
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Expected valid ip address. Got: " + address
        where:
            address << invalidIpV4 + invalidIpV6
    }

    @Unroll
    def "should validate valid inet v4 address: #address"() {
        when:
            InetAddressValidator.validateInetV4Address(address)
        then:
            noExceptionThrown()
        where:
            address << validIpV4
    }

    @Unroll
    def "should validate invalid inet v4 address: #address"() {
        when:
            InetAddressValidator.validateInetV4Address(address)
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Expected valid ip v4 address. Got: " + address
        where:
            address << invalidIpV4 + validIpV6 + invalidIpV6
    }

    @Unroll
    def "should validate valid inet v6 address: #address"() {
        when:
            InetAddressValidator.validateInetV6Address(address)
        then:
            noExceptionThrown()
        where:
            address << validIpV6
    }

    @Unroll
    def "should validate invalid inet v6 address: #address"() {
        when:
            InetAddressValidator.validateInetV6Address(address)
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.message == "Expected valid ip v6 address. Got: " + address
        where:
            address << invalidIpV6 + validIpV4 + invalidIpV4
    }
}
