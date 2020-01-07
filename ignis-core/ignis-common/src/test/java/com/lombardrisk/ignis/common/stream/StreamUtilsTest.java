package com.lombardrisk.ignis.common.stream;

import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamUtilsTest {

    @Test
    public void concat_MergesAllStreamsInOrder() {
        Stream<Integer> concatenated = StreamUtils.concat(
                Stream.of(1, 2, 3, 4),
                Stream.of(7, 8, 9),
                Stream.of(5, 6));

        assertThat(concatenated)
                .containsExactly(1, 2, 3, 4, 7, 8, 9, 5, 6);
    }
}
