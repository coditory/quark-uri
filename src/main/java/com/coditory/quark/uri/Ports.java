package com.coditory.quark.uri;

public final class Ports {
    public final static int SCHEME_DEFAULT_PORT_NUMBER = -1;
    public final static int MIN_PORT_VALUE = 0;
    public final static int MAX_PORT_VALUE = 65535;

    public static boolean isValidPortNumberOrSchemeDefault(int port) {
        return port >= SCHEME_DEFAULT_PORT_NUMBER && port <= MAX_PORT_VALUE;
    }

    public static boolean isValidPortNumber(int port) {
        return port >= MIN_PORT_VALUE && port <= MAX_PORT_VALUE;
    }

    public static boolean isSchemeDefault(int port) {
        return port == SCHEME_DEFAULT_PORT_NUMBER;
    }

    public static void validatePortNumberOrSchemeDefault(int port) {
        if (!isValidPortNumberOrSchemeDefault(port)) {
            String message = String.format("Expected port number in range [%d, %d]. Got: %d",
                    SCHEME_DEFAULT_PORT_NUMBER, MAX_PORT_VALUE, port);
            throw new IllegalArgumentException(message);
        }
    }

    public static void validatePortNumber(int port) {
        if (!isValidPortNumber(port)) {
            String message = String.format("Expected port number in range [%d, %d]. Got: %d",
                    MIN_PORT_VALUE, MAX_PORT_VALUE, port);
            throw new IllegalArgumentException(message);
        }
    }

    public static int getNextAvailable() {
        return PortsAvailable.getNextAvailable();
    }

    public static int getNextAvailable(int fromPort) {
        return PortsAvailable.getNextAvailable(fromPort);
    }

    private Ports() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }
}
