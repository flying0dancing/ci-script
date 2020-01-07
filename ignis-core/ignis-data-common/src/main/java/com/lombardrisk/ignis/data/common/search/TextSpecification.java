package com.lombardrisk.ignis.data.common.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Getter
@Builder
@AllArgsConstructor
public class TextSpecification<R> implements SearchSpecification<R, String> {

    private static final long serialVersionUID = 1755796481345031942L;

    private final String key;
    private final FilterOption option;
    private final String value;

    @Override
    public Path<String> getPath(final Root<R> root) {
        return root.get(key);
    }

    @Override
    public Predicate toPredicate(
            final Root<R> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {

        switch (option) {
            case EQUALS:
                return equal(root, criteriaBuilder);

            case NOT_EQUAL:
                return notEqual(root, criteriaBuilder);

            case CONTAINS:
                return contains(root, criteriaBuilder);

            case NOT_CONTAINS:
                return doesNotContain(root, criteriaBuilder);

            case STARTS_WITH:
                return startsWith(root, criteriaBuilder);

            case ENDS_WITH:
                return endsWith(root, criteriaBuilder);

            default:
                throw new UnsupportedOperationException(unsupportedOperatorFailure());
        }
    }

    private Predicate contains(final Root<R> root, final CriteriaBuilder cb) {
        return cb.like(getPath(root), "%" + value + "%");
    }

    private Predicate doesNotContain(final Root<R> root, final CriteriaBuilder cb) {
        return cb.notLike(getPath(root), "%" + value + "%");
    }

    private Predicate startsWith(final Root<R> root, final CriteriaBuilder cb) {
        return cb.like(getPath(root), value + "%");
    }

    private Predicate endsWith(final Root<R> root, final CriteriaBuilder cb) {
        return cb.like(getPath(root), "%" + value);
    }
}
