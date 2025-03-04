package com.coditory.quark.uri;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.coditory.quark.uri.Strings.isNullOrBlank;

public final class InetAddressValidator {
    private InetAddressValidator() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    private static final int MAX_BYTE = 128;
    private static final int IPV4_MAX_OCTET_VALUE = 255;
    private static final int MAX_UNSIGNED_SHORT = 0xffff;
    private static final int BASE_16 = 16;
    private static final Pattern IPV4_REGEX = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    private static final int IPV6_MAX_HEX_GROUPS = 8;
    private static final int IPV6_MAX_HEX_DIGITS_PER_GROUP = 4;

    public static void validateInetAddress(String inetAddress) {
        if (!isValidInetV4Address(inetAddress) && !isValidInetV6Address(inetAddress)) {
            throw new IllegalArgumentException("Expected valid ip address. Got: " + inetAddress);
        }
    }

    public static void validateInetV4Address(String inetAddress) {
        if (!isValidInetV4Address(inetAddress)) {
            throw new IllegalArgumentException("Expected valid ip v4 address. Got: " + inetAddress);
        }
    }

    public static void validateInetV6Address(String inetAddress) {
        if (!isValidInetV6Address(inetAddress)) {
            throw new IllegalArgumentException("Expected valid ip v6 address. Got: " + inetAddress);
        }
    }

    public static boolean isValidInetAddress(String inetAddress) {
        return isValidInetV4Address(inetAddress) || isValidInetV6Address(inetAddress);
    }

    public static boolean isValidInetV4Address(@Nullable String inet4Address) {
        if (inet4Address == null || inet4Address.isBlank()) {
            return false;
        }
        Matcher matcher = IPV4_REGEX.matcher(inet4Address);
        if (!matcher.matches()) {
            return false;
        }
        for (int j = 0; j < matcher.groupCount(); j++) {
            String ipSegment = matcher.group(j + 1);
            if (isNullOrBlank(ipSegment)) {
                return false;
            }
            if (ipSegment.length() > 1 && ipSegment.startsWith("0")) {
                return false;
            }
            Integer iIpSegment = parseIntegerOrNull(ipSegment);
            if (iIpSegment == null || iIpSegment > IPV4_MAX_OCTET_VALUE) {
                return false;
            }
        }
        return true;
    }

    private static Integer parseIntegerOrNull(String text) {
        if (text == null) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static boolean isValidInetV6Address(String inet6Address) {
        if (inet6Address == null || inet6Address.isBlank()) {
            return false;
        }
        String[] parts;
        parts = inet6Address.split("/", -1);
        if (parts.length > 2) {
            return false;
        }
        if (parts.length == 2) {
            if (parts[1].matches("\\d{1,3}")) {
                int bits = Integer.parseInt(parts[1]);
                if (bits < 0 || bits > MAX_BYTE) {
                    return false;
                }
            } else {
                return false;
            }
        }
        parts = parts[0].split("%", -1);
        if (parts.length > 2) {
            return false;
        } else if (parts.length == 2) {
            if (!parts[1].matches("[^\\s/%]+")) {
                return false;
            }
        }
        inet6Address = parts[0];
        boolean containsCompressedZeroes = inet6Address.contains("::");
        if (containsCompressedZeroes && (inet6Address.indexOf("::") != inet6Address.lastIndexOf("::"))) {
            return false;
        }
        if ((inet6Address.startsWith(":") && !inet6Address.startsWith("::"))
                || (inet6Address.endsWith(":") && !inet6Address.endsWith("::"))) {
            return false;
        }
        String[] octets = inet6Address.split(":");
        if (containsCompressedZeroes) {
            List<String> octetList = new ArrayList<>(Arrays.asList(octets));
            if (inet6Address.endsWith("::")) {
                octetList.add("");
            } else if (inet6Address.startsWith("::") && !octetList.isEmpty()) {
                octetList.removeFirst();
            }
            octets = octetList.toArray(new String[0]);
        }
        if (octets.length > IPV6_MAX_HEX_GROUPS) {
            return false;
        }
        int validOctets = 0;
        int emptyOctets = 0; // consecutive empty chunks
        for (int index = 0; index < octets.length; index++) {
            String octet = octets[index];
            if (octet.isEmpty()) {
                emptyOctets++;
                if (emptyOctets > 1) {
                    return false;
                }
            } else {
                emptyOctets = 0;
                if (index == octets.length - 1 && octet.contains(".")) {
                    if (!isValidInetV4Address(octet)) {
                        return false;
                    }
                    validOctets += 2;
                    continue;
                }
                if (octet.length() > IPV6_MAX_HEX_DIGITS_PER_GROUP) {
                    return false;
                }
                int octetInt = 0;
                try {
                    octetInt = Integer.parseInt(octet, BASE_16);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (octetInt < 0 || octetInt > MAX_UNSIGNED_SHORT) {
                    return false;
                }
            }
            validOctets++;
        }
        return validOctets <= IPV6_MAX_HEX_GROUPS
                && (validOctets == IPV6_MAX_HEX_GROUPS || containsCompressedZeroes);
    }
}
