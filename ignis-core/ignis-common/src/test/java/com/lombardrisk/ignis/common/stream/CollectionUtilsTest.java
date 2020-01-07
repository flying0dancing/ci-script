package com.lombardrisk.ignis.common.stream;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

public class CollectionUtilsTest {

    @Test
    public void concat_HeadAndTail_ReturnsCollectionOfCorrectOrderAndType() {
        Collection<String> concatenated = CollectionUtils.concat(Arrays.asList("A", "B", "C"), "D", LinkedHashSet::new);

        assertThat(concatenated)
                .isInstanceOf(LinkedHashSet.class)
                .containsExactly("A", "B", "C", "D");
    }

    @Test
    public void orEmpty_InputIsNull_ReturnsEmptyList() {
        List<String> list = null;
        assertThat(CollectionUtils.orEmpty(list))
                .isEqualTo(emptyList());
    }

    @Test
    public void orEmpty_InputContainsValues_ReturnsOriginal() {
        List<String> list = Arrays.asList("A", "B", "C");
        assertThat(CollectionUtils.orEmpty(list))
                .isEqualTo(list)
                .isSameAs(list);
    }
}
