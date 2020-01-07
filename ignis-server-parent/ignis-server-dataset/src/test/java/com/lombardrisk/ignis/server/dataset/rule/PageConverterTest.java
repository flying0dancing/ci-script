package com.lombardrisk.ignis.server.dataset.rule;

import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.server.product.util.PageConverter;
import org.junit.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PageConverterTest {

    private final PageConverter converter = new PageConverter();

    @Test
    public void apply_FirstPage_ReturnsPageViewWithPageOne() {

        Page page = converter.apply(
                new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 2), 3));

        assertThat(page.getNumber())
                .isEqualTo(1);
    }

    @Test
    public void apply_PageSize_ReturnsPageViewWithPageSize() {

        Page page = converter.apply(
                new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 2), 3));

        assertThat(page.getSize())
                .isEqualTo(2);
    }

    @Test
    public void apply_TotalElements_ReturnsPageViewWithTotalElements() {

        Page page = converter.apply(
                new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 2), 3));

        assertThat(page.getTotalElements())
                .isEqualTo(3);
    }

    @Test
    public void apply_TotalPages_ReturnsPageViewWithTotalPages() {

        Page page = converter.apply(
                new PageImpl<>(Collections.emptyList(), PageRequest.of(1, 2), 3));

        assertThat(page.getTotalPages())
                .isEqualTo(2);
    }
}
