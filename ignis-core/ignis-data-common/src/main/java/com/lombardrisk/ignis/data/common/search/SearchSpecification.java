package com.lombardrisk.ignis.data.common.search;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface SearchSpecification<R, T> extends Specification<R> {

    Path<T> getPath(Root<R> root);

    String getKey();

    FilterOption getOption();

    T getValue();

    default Predicate equal(Root<R> root, CriteriaBuilder cb) {
        Path<T> path = getPath(root);
        return cb.equal(path, getValue());
    }

    default Predicate notEqual(Root<R> root, CriteriaBuilder cb) {
        Path<T> path = getPath(root);
        return cb.notEqual(path, getValue());
    }

    default String unsupportedOperatorFailure() {
        return String.format("Unsupported search option '%s' for property '%s'", getOption(), getKey());
    }
}
