package com.lombardrisk.ignis.common.stream;

import lombok.experimental.UtilityClass;

import java.util.function.Function;
import java.util.stream.Stream;

@UtilityClass
public class StreamUtils {

    public static <T, R1 extends T, R2 extends T, R3 extends T> Stream<T> concat(
            final Stream<R1> stream1,
            final Stream<R2> stream2,
            final Stream<R3> stream3) {
        return Stream.of(stream1, stream2, stream3)
                .flatMap(Function.identity());
    }
}
