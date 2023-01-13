package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.BitSet;

import static com.coditory.quark.uri.Preconditions.expectNonNull;
import static com.coditory.quark.uri.UriRfcCharacters.URI_UNRESERVED;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class PercentCodec {
    public static final PercentCodec PERCENT_CODEC = PercentCodec.builder()
            .decodeSpaceAsPlus(false)
            .charset(UTF_8)
            .safeCharactersAsUriUnreserved()
            .build();

    public static final PercentCodec PERCENT_PLUS_CODEC = PercentCodec.builder()
            .decodeSpaceAsPlus(true)
            .charset(UTF_8)
            .safeCharactersAsUriUnreserved()
            .build();

    public static String encodeUriComponent(String component) {
        return PERCENT_CODEC.encode(component);
    }

    public static String decodeUriComponent(String component) {
        return PERCENT_CODEC.decode(component);
    }

    public static String encodeUriComponentWithPlusAsSpace(String component) {
        return PERCENT_PLUS_CODEC.encode(component);
    }

    public static String decodeUriComponentWithPlusAsSpace(String component) {
        return PERCENT_PLUS_CODEC.decode(component);
    }

    private final PercentEncoder encoder;
    private final PercentDecoder decoder;

    PercentCodec(PercentEncoder encoder, PercentDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @NotNull
    public String encode(@NotNull String input) {
        return encoder.encode(input);
    }

    public boolean encode(@NotNull String input, @NotNull StringBuilder out) {
        return encoder.encode(input, out);
    }

    public boolean encode(@NotNull String input, @NotNull StringBuilder out, @NotNull Charset charset) {
        return encoder.encode(input, out, charset);
    }

    @NotNull
    public String decode(@NotNull String input) {
        return decoder.decode(input);
    }

    public boolean decode(@NotNull String input, @NotNull StringBuilder out) {
        return decoder.decode(input, out);
    }

    public boolean decode(@NotNull String input, @NotNull StringBuilder out, @NotNull Charset charset) {
        return decoder.decode(input, out, charset);
    }

    @NotNull
    public static PercentCodecBuilder builder() {
        return new PercentCodecBuilder();
    }

    public static class PercentCodecBuilder {
        private BitSet safeCharacters = BitSets.of(URI_UNRESERVED);
        private boolean decodeSpaceAsPlus = false;
        private boolean encodeSpaceAsPlus = false;
        private Charset charset = UTF_8;

        @NotNull
        public PercentCodecBuilder addSafeCharacters(@NotNull String safeCharacters) {
            expectNonNull(safeCharacters, "safeCharacters");
            this.safeCharacters.or(BitSets.of(safeCharacters));
            return this;
        }

        @NotNull
        public PercentCodecBuilder safeCharacters(@NotNull String safeCharacters) {
            expectNonNull(safeCharacters, "safeCharacters");
            this.safeCharacters = BitSets.of(safeCharacters);
            return this;
        }

        @NotNull
        public PercentCodecBuilder safeCharactersAsUriUnreserved() {
            return safeCharacters(URI_UNRESERVED);
        }

        @NotNull
        public PercentCodecBuilder decodeSpaceAsPlus(boolean spaceAsPlus) {
            this.decodeSpaceAsPlus = spaceAsPlus;
            return this;
        }

        @NotNull
        public PercentCodecBuilder encodeSpaceAsPlus(boolean spaceAsPlus) {
            this.encodeSpaceAsPlus = spaceAsPlus;
            return this;
        }

        @NotNull
        public PercentCodecBuilder spaceAsPlus(boolean spaceAsPlus) {
            this.decodeSpaceAsPlus = spaceAsPlus;
            this.encodeSpaceAsPlus = spaceAsPlus;
            return this;
        }

        @NotNull
        public PercentCodecBuilder charset(Charset charset) {
            expectNonNull(charset, "charset");
            this.charset = charset;
            return this;
        }

        @NotNull
        public PercentCodec build() {
            if (encodeSpaceAsPlus || decodeSpaceAsPlus) {
                safeCharacters = BitSets.set(safeCharacters, '+', false);
            }
            PercentEncoder encoder = PercentEncoder.builder()
                    .safeCharacters(safeCharacters)
                    .spaceAsPlus(encodeSpaceAsPlus)
                    .charset(charset)
                    .build();
            PercentDecoder decoder = PercentDecoder.builder()
                    .spaceAsPlus(decodeSpaceAsPlus)
                    .charset(charset)
                    .build();
            return new PercentCodec(encoder, decoder);
        }
    }
}
