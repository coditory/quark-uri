package com.coditory.quark.uri;

import java.nio.charset.Charset;

import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static java.nio.charset.StandardCharsets.UTF_8;

final class PercentDecoder {
    private final Charset charset;
    private final boolean spaceAsPlus;

    private PercentDecoder(boolean spaceAsPlus, Charset charset) {
        this.spaceAsPlus = spaceAsPlus;
        this.charset = expectNonNull(charset, "charset");
    }

    String decode(String text) {
        StringBuilder dst = new StringBuilder();
        decode(text, dst);
        return dst.toString();
    }

    boolean decode(String text, StringBuilder dst) {
        return decode(text, dst, charset);
    }

    boolean decode(String text, StringBuilder dst, Charset charset) {
        expectNonNull(dst, "dst");
        expectNonNull(text, "text");
        expectNonNull(charset, "charset");
        boolean needToChange = false;
        int length = text.length();
        StringBuilder sb = new StringBuilder(length > 500 ? length / 2 : length);
        int i = 0;
        byte[] bytes = null;
        while (i < length) {
            char c = text.charAt(i);
            switch (c) {
                case '+' -> {
                    if (!spaceAsPlus) {
                        sb.append('+');
                    } else {
                        sb.append(' ');
                        needToChange = true;
                    }
                    i++;
                }
                case '%' -> {
                    try {
                        if (bytes == null) {
                            bytes = new byte[(length - i) / 3];
                        }
                        int pos = 0;
                        while ((i + 2) < length && c == '%') {
                            int v = Integer.parseInt(text, i + 1, i + 3, 16);
                            if (v < 0) {
                                throw new IllegalArgumentException("Illegal hex characters in escape (%) pattern - negative value");
                            }
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < length) {
                                c = text.charAt(i);
                            }
                        }
                        if (i < length && c == '%') {
                            throw new IllegalArgumentException("Incomplete trailing escape (%) pattern");
                        }
                        sb.append(new String(bytes, 0, pos, charset));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Illegal hex characters in escape (%) pattern - " + e.getMessage());
                    }
                    needToChange = true;
                }
                default -> {
                    sb.append(c);
                    i++;
                }
            }
        }
        dst.append(sb);
        return needToChange;
    }

    static PercentDecoderBuilder builder() {
        return new PercentDecoderBuilder();
    }

    static class PercentDecoderBuilder {
        private boolean spaceAsPlus = false;
        private Charset charset = UTF_8;

        public PercentDecoderBuilder spaceAsPlus(boolean spaceAsPlus) {
            this.spaceAsPlus = spaceAsPlus;
            return this;
        }

        public PercentDecoderBuilder charset(Charset charset) {
            expectNonNull(charset, "charset");
            this.charset = charset;
            return this;
        }

        public PercentDecoder build() {
            return new PercentDecoder(spaceAsPlus, charset);
        }
    }
}
