package com.coditory.quark.uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coditory.quark.uri.Preconditions.expectNonNull;

final class Throwables {
    private Throwables() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    @SuppressWarnings("unchecked")
    static <T extends Throwable> T getRootCauseOfType(Throwable throwable, Class<T> type) {
        expectNonNull(throwable, "throwable");
        expectNonNull(type, "type");
        List<Throwable> list = getCauses(throwable);
        Collections.reverse(list);
        Throwable result = list.stream()
                .filter(type::isInstance)
                .findFirst()
                .orElse(null);
        return (T) result;
    }

    static List<Throwable> getCauses(Throwable throwable) {
        expectNonNull(throwable, "throwable");
        List<Throwable> list = new ArrayList<>();
        Set<Throwable> visited = new HashSet<>();
        while (throwable != null && !visited.contains(throwable)) {
            list.add(throwable);
            visited.add(throwable);
            throwable = throwable.getCause();
        }
        return list;
    }
}
