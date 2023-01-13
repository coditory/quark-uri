package com.coditory.quark.uri

import spock.lang.Specification
import spock.lang.Unroll

class PortsSpec extends Specification {
    def "should find two available ports"() {
        when:
            int port = Ports.nextAvailablePort()
        then:
            port >= 0 && port < 65535
    }

    @Unroll
    def "should validate valid port number: #port"() {
        when:
            Ports.validatePortNumber(port)
        then:
            noExceptionThrown()
        where:
            port << [0, 1, 1024, 65535]
    }

    @Unroll
    def "should validate invalid port number: #port"() {
        when:
            Ports.validatePortNumber(port)
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.getMessage() == "Expected port number in range [0, 65535]. Got: " + port
        where:
            port << [-1, 65536]
    }

    @Unroll
    def "should validate valid port number or scheme default: #port"() {
        when:
            Ports.validatePortNumberOrSchemeDefault(port)
        then:
            noExceptionThrown()
        where:
            port << [-1, 1, 1024, 65535]
    }

    @Unroll
    def "should validate invalid port number or scheme default: #port"() {
        when:
            Ports.validatePortNumberOrSchemeDefault(port)
        then:
            IllegalArgumentException e = thrown(IllegalArgumentException)
            e.getMessage() == "Expected port number in range [-1, 65535]. Got: " + port
        where:
            port << [-2, 65536]
    }
}
