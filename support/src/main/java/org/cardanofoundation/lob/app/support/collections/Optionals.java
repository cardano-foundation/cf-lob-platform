package org.cardanofoundation.lob.app.support.collections;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.BiFunction;

@UtilityClass
public class Optionals {

    public <T, U, R> Optional<R> zip(Optional<T> a, Optional<U> b, BiFunction<T, U, R> zipper) {
        return a.flatMap(va -> b.map(vb -> zipper.apply(va, vb)));
    }

}
