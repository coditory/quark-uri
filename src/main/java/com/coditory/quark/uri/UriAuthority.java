package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

import static com.coditory.quark.uri.Nullable.mapNotNull;
import static com.coditory.quark.uri.Ports.SCHEME_DEFAULT_PORT_NUMBER;
import static com.coditory.quark.uri.Ports.validatePortNumberOrSchemeDefault;
import static com.coditory.quark.uri.Preconditions.expectNoWhitespaces;
import static com.coditory.quark.uri.Preconditions.expectNonEmpty;

public final class UriAuthority {
    private static final UriAuthority EMPTY = new UriAuthority(null, null, SCHEME_DEFAULT_PORT_NUMBER);

    @NotNull
    public static UriAuthority empty() {
        return EMPTY;
    }

    @NotNull
    public static UriAuthority of(String userInfo, String hostname, int port) {
        if (hostname != null) {
            expectNoWhitespaces(hostname, "hostname");
        }
        if (userInfo != null) {
            expectNonEmpty(userInfo, "userInfo");
        }
        validatePortNumberOrSchemeDefault(port);
        if (userInfo == null && hostname == null && Ports.isSchemeDefault(port)) {
            return empty();
        }
        return new UriAuthority(
                userInfo,
                mapNotNull(hostname, h -> h.toLowerCase(Locale.ROOT)),
                port
        );
    }

    private final String userInfo;
    private final String hostname;
    private final int port;

    private UriAuthority(String userInfo, String hostname, int port) {
        this.userInfo = userInfo;
        this.hostname = hostname;
        this.port = port;
    }

    @Nullable
    public String getUserInfo() {
        return userInfo;
    }

    @Nullable
    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public boolean usesSchemeDefaultPort() {
        return port == SCHEME_DEFAULT_PORT_NUMBER;
    }

    public boolean isEmpty() {
        return this.equals(EMPTY);
    }

    @Override
    public String toString() {
        return "UriAuthority{" +
                "userInfo='" + userInfo + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UriAuthority that = (UriAuthority) o;
        return port == that.port
                && Objects.equals(userInfo, that.userInfo)
                && Objects.equals(hostname, that.hostname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userInfo, hostname, port);
    }
}
