package com.coditory.quark.uri;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.util.BitSet;

import static com.coditory.quark.uri.BitSets.unmodifiableBitSet;
import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static com.coditory.quark.uri.UriRfcCharacters.URI_UNRESERVED;
import static java.nio.charset.StandardCharsets.UTF_8;

final class PercentEncoder {
    private static final int CASE_DIFF = ('a' - 'A');
    private final boolean spaceAsPlus;
    private final Charset charset;
    private final BitSet safeCharacters;

    private PercentEncoder(BitSet safeCharacters, boolean spaceAsPlus, Charset charset) {
        this.spaceAsPlus = spaceAsPlus;
        this.charset = expectNonNull(charset, "charset");
        this.safeCharacters = expectNonNull(safeCharacters, "safeCharacters");
    }

    String encode(String text) {
        StringBuilder dst = new StringBuilder();
        encode(text, dst);
        return dst.toString();
    }

    boolean encode(String text, StringBuilder dst) {
        return encode(text, dst, charset);
    }

    boolean encode(String text, StringBuilder dst, Charset charset) {
        expectNonNull(dst, "dst");
        expectNonNull(text, "text");
        expectNonNull(charset, "charset");
        boolean needToChange = false;
        StringBuilder out = new StringBuilder(text.length());
        CharArrayWriter charArrayWriter = new CharArrayWriter();

        for (int i = 0; i < text.length(); ) {
            int cp = text.codePointAt(i);
            if (cp == ' ' && spaceAsPlus) {
                out.append('+');
                needToChange = true;
                i++;
            } else if (safeCharacters.get(cp) && (cp != '+' || !spaceAsPlus)) {
                out.appendCodePoint(cp);
                i += Character.charCount(cp);
            } else {
                do {
                    char c = text.charAt(i);
                    charArrayWriter.write(c);
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        if ((i + 1) < text.length()) {
                            char d = text.charAt(i + 1);
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < text.length() && !safeCharacters.get(text.codePointAt(i)));
                charArrayWriter.flush();
                byte[] bytes = new String(charArrayWriter.toCharArray())
                        .getBytes(charset);
                for (byte aByte : bytes) {
                    out.append('%');
                    char c = Character.forDigit((aByte >> 4) & 0xF, 16);
                    if (Character.isLetter(c)) {
                        c -= CASE_DIFF;
                    }
                    out.append(c);
                    c = Character.forDigit(aByte & 0xF, 16);
                    if (Character.isLetter(c)) {
                        c -= CASE_DIFF;
                    }
                    out.append(c);
                }
                charArrayWriter.reset();
                needToChange = true;
            }
        }
        dst.append(out);
        return needToChange;
    }

    static PercentEncoderBuilder builder() {
        return new PercentEncoderBuilder();
    }

    static class PercentEncoderBuilder {
        private BitSet safeCharacters = BitSets.of(URI_UNRESERVED);
        private boolean spaceAsPlus = false;
        private Charset charset = UTF_8;

        PercentEncoderBuilder addSafeCharacters(String safeCharacters) {
            expectNonNull(safeCharacters, "safeCharacters");
            return safeCharacters(BitSets.of(safeCharacters));
        }

        PercentEncoderBuilder safeCharactersAsUriUnreserved() {
            return safeCharacters(URI_UNRESERVED);
        }

        PercentEncoderBuilder safeCharacters(BitSet safeCharacters) {
            expectNonNull(safeCharacters, "safeCharacters");
            this.safeCharacters = unmodifiableBitSet(safeCharacters);
            return this;
        }

        PercentEncoderBuilder safeCharacters(String safeCharacters) {
            expectNonNull(safeCharacters, "safeCharacters");
            return safeCharacters(BitSets.of(safeCharacters));
        }

        PercentEncoderBuilder spaceAsPlus(boolean spaceAsPlus) {
            this.spaceAsPlus = spaceAsPlus;
            return this;
        }

        PercentEncoderBuilder charset(Charset charset) {
            expectNonNull(charset, "charset");
            this.charset = charset;
            return this;
        }

        PercentEncoder build() {
            if (spaceAsPlus) {
                safeCharacters = BitSets.set(safeCharacters, '+', false);
            }
            return new PercentEncoder(safeCharacters, spaceAsPlus, charset);
        }
    }
}
