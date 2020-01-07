package com.lombardrisk.ignis.server.product.util;

import com.lombardrisk.ignis.client.core.view.PagedView;
import org.springframework.data.domain.Page;

public class PageToViewConverter  {

    private final PageConverter pageConverter;

    public PageToViewConverter(final PageConverter pageConverter) {
        this.pageConverter = pageConverter;
    }

    public <T> PagedView<T> apply(final Page<T> page) {
        return new PagedView<>(page.getContent(), pageConverter.apply(page));
    }
}
