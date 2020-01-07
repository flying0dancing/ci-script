package com.lombardrisk.ignis.server.product.util;

import io.vavr.Function1;
import org.springframework.data.domain.Page;

public class PageConverter implements Function1<Page, com.lombardrisk.ignis.client.core.page.response.Page> {

    @Override
    public com.lombardrisk.ignis.client.core.page.response.Page apply(final Page page) {
        return com.lombardrisk.ignis.client.core.page.response.Page.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements((int) page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }
}
