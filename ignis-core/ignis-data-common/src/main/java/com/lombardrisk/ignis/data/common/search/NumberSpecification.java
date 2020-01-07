package com.lombardrisk.ignis.data.common.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

@Getter
@Builder
@AllArgsConstructor
public class NumberSpecification<R> implements ComparableSpecification<R, Double> {

    private static final long serialVersionUID = -6207749872499177173L;

    private final String key;
    private final FilterOption option;
    private final Double value;
    private final Double to;

    @Override
    public Path<Double> getPath(final Root<R> root) {
        return root.get(key);
    }
}
