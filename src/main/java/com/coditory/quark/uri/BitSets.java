package com.coditory.quark.uri;

import org.jetbrains.annotations.NotNull;

import java.util.BitSet;

import static com.coditory.quark.uri.Preconditions.expectNonNull;

final class BitSets {
    private BitSets() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static BitSet of(String text) {
        expectNonNull(text, "text");
        if (text.length() == 0) {
            return new BitSet(0);
        }
        int max = text.codePoints().max().getAsInt();
        BitSet bitSet = new BitSet(max + 1);
        text.codePoints()
                .forEach(bitSet::set);
        return bitSet;
    }

    public static BitSet set(BitSet bitSet, int bitIndex, boolean value) {
        expectNonNull(bitSet, "bitSet");
        BitSet result = new BitSet(Math.max(bitSet.length(), bitIndex + 1));
        result.or(bitSet);
        result.set(bitIndex, value);
        return result;
    }

    public static BitSet unmodifiableBitSet(BitSet bitSet) {
        expectNonNull(bitSet, "bitSet");
        return new UnmodifiableBitSet(bitSet);
    }

    static final class UnmodifiableBitSet extends BitSet {
        public UnmodifiableBitSet(@NotNull BitSet original) {
            expectNonNull(original, "original");
            super.or(original);
        }

        @Override
        public void flip(int bitIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flip(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int bitIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int bitIndex, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int fromIndex, int toIndex, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear(int bitIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear(int fromIndex, int toIndex) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void and(@NotNull BitSet set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void or(@NotNull BitSet set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void xor(@NotNull BitSet set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void andNot(@NotNull BitSet set) {
            throw new UnsupportedOperationException();
        }
    }
}
