package com.lombardrisk.ignis.common.stream;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SeqUtilsTest {

    @Test
    public void flatMapToList_EmptyCollection_ReturnsEmptyList() {
        Seq<java.util.List<String>> seqOfLists = List.empty();

        assertThat(SeqUtils.flatMapToList(seqOfLists))
                .isEmpty();
    }

    @Test
    public void flatMapToList_NonEmptyCollection_ReturnsFlattenedList() {
        Seq<java.util.List<String>> seqOfLists = List.of(
                Arrays.asList("A", "B", "C"),
                Arrays.asList("D", "E"),
                Collections.singletonList("F"));

        assertThat(SeqUtils.flatMapToList(seqOfLists))
                .containsExactly("A", "B", "C", "D", "E", "F");
    }
}