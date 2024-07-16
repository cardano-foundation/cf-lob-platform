package org.cardanofoundation.lob.app.support.collections;

import java.util.Optional;
import java.util.function.BiFunction;

public final class Optionals {

    public static <T, U, R> Optional<R> zip(Optional<T> a, Optional<U> b, BiFunction<T, U, R> zipper) {
        return a.flatMap(va -> b.map(vb -> zipper.apply(va, vb)));
    }

}
