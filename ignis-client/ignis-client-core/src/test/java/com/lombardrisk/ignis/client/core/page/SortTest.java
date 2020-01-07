package com.lombardrisk.ignis.client.core.page;

import com.lombardrisk.ignis.client.core.page.request.Sort;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class SortTest {

    @Test
    public void toParameterMap_Asc() {
        Sort sort = Sort.builder()
                .field("name")
                .direction(Sort.Direction.ASC)
                .build();

        assertThat(sort.toParameterMap())
                .contains(
                        entry("sort", "name,asc"));
    }

    @Test
    public void toParameterMap_Desc() {
        Sort sort = Sort.builder()
                .field("id")
                .direction(Sort.Direction.DESC)
                .build();

        assertThat(sort.toParameterMap())
                .contains(
                        entry("sort", "id,desc"));
    }
}