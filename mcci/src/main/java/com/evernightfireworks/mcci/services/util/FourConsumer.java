package com.evernightfireworks.mcci.services.util;

import java.util.Objects;

@FunctionalInterface
public interface FourConsumer<T, U, V, W> {
    void accept(T t, U u, V v, W w);
    default FourConsumer<T, U, V, W> andThen(FourConsumer<? super T, ? super U, ? super V, ? super W> after) {
        Objects.requireNonNull(after);

        return (l, r, s, t) -> {
            accept(l, r, s, t);
            after.accept(l, r, s, t);
        };
    }
}