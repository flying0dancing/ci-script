package com.lombardrisk.ignis.client.core.page;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.page.request.Sort;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class PageRequestTest {

    @Test
    public void toParameterMap() {
        PageRequest page = PageRequest.builder()
                .page(0)
                .size(100)
                .build();

        assertThat(page.toParameterMap())
                .contains(
                        entry("page", 0),
                        entry("size", 100));
    }

    @Test
    public void toParameterMap_Sorted_ReturnsSortedParams() {
        PageRequest page = PageRequest.builder()
                .page(0)
                .size(100)
                .sort(Sort.builder()
                        .field("name")
                        .direction(Sort.Direction.ASC)
                        .build())
                .build();

        assertThat(page.toParameterMap())
                .contains(
                        entry("sort", "name,asc"));
    }
}