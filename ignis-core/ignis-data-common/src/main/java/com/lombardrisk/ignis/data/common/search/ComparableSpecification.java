package com.lombardrisk.ignis.data.common.search;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface ComparableSpecification<R, T extends Comparable<T>> extends SearchSpecification<R, T> {

    T getTo();

    @Override
    default Predicate toPredicate(
            final Root<R> root, final CriteriaQuery<?> criteriaQuery, final CriteriaBuilder criteriaBuilder) {

        switch (getOption()) {
            case EQUALS:
                return equal(root, criteriaBuilder);

            case NOT_EQUAL:
                return notEqual(root, criteriaBuilder);

            case LESS_THAN:
                return lessThan(root, criteriaBuilder);

            case LESS_THAN_OR_EQUAL:
                return lessThanOrEqualTo(root, criteriaBuilder);

            case GREATER_THAN:
                return greaterThan(root, criteriaBuilder);

            case GREATER_THAN_OR_EQUAL:
                return greaterThanOrEqualTo(root, criteriaBuilder);

            case IN_RANGE:
                return between(root, criteriaBuilder);

            default:
                throw new UnsupportedOperationException(unsupportedOperatorFailure());
        }
    }

    default Predicate lessThan(final Root<R> root, final CriteriaBuilder cb) {
        return cb.lessThan(getPath(root), getValue());
    }

    default Predicate lessThanOrEqualTo(final Root<R> root, final CriteriaBuilder cb) {
        return cb.lessThanOrEqualTo(getPath(root), getValue());
    }

    default Predicate greaterThan(final Root<R> root, final CriteriaBuilder cb) {
        return cb.greaterThan(getPath(root), getValue());
    }

    default Predicate greaterThanOrEqualTo(final Root<R> root, final CriteriaBuilder cb) {
        return cb.greaterThanOrEqualTo(getPath(root), getValue());
    }

    default Predicate between(final Root<R> root, final CriteriaBuilder cb) {
        return cb.between(getPath(root), getValue(), getTo());
    }
}
