package com.lombardrisk.ignis.client.core.fixture;

import com.lombardrisk.ignis.client.core.page.request.PageRequest;
import com.lombardrisk.ignis.client.core.page.request.Sort;
import com.lombardrisk.ignis.client.core.page.response.Page;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ClientCore {

    @UtilityClass
    public static class Populated {

        public static Sort.SortBuilder sort() {
            return Sort.builder()
                    .field("name")
                    .direction(Sort.Direction.ASC);
        }

        public static PageRequest.PageRequestBuilder pageRequest() {
            return PageRequest.builder()
                    .page(1)
                    .size(10)
                    .sort(sort().build());
        }

        public static Page.PageBuilder page() {
            return Page.builder()
                    .number(0)
                    .size(10)
                    .totalPages(10)
                    .totalElements(100);
        }
    }
}
