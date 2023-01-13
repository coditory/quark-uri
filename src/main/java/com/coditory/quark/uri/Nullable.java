package com.coditory.quark.uri;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.coditory.quark.uri.Preconditions.expectNonNull;

final class Nullable {
    private Nullable() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    static <T, R> R mapNotNull(T object, Function<T, R> mapper) {
        expectNonNull(mapper, "mapper");
        return object != null
                ? mapper.apply(object)
                : null;
    }

    static <T, R> R mapNotNull(T object, Function<T, R> mapper, R defaultValue) {
        expectNonNull(mapper, "mapper");
        return object != null
                ? mapper.apply(object)
                : defaultValue;
    }

    static <T> void onNotNull(T object, Consumer<T> consumer) {
        expectNonNull(consumer, "consumer");
        if (object != null) {
            consumer.accept(object);
        }
    }
}
