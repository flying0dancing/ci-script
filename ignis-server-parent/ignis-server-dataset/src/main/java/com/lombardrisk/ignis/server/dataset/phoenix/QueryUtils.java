package com.lombardrisk.ignis.server.dataset.phoenix;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Pageable;

@UtilityClass
public class QueryUtils {

    public static String paginatedQueryFromPageable(final Pageable pageRequest) {
        return " LIMIT " + pageRequest.getPageSize() + " OFFSET " + pageRequest.getOffset();
    }
}
