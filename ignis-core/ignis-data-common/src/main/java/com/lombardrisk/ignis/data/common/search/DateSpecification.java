package com.lombardrisk.ignis.data.common.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
public class DateSpecification<R> implements ComparableSpecification<R, Date> {

    private static final long serialVersionUID = -6207749872499177173L;

    private final String key;
    private final FilterOption option;
    private final Date value;
    private final Date to;

    @Override
    public Path<Date> getPath(final Root<R> root) {
        return root.get(key);
    }
}
