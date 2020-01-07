package com.lombardrisk.ignis.data.common.search;

import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

@AllArgsConstructor
public abstract class SpecificationBuilder<T> {

    protected abstract Validation<CRUDFailure, Specification<T>> build(Filter filter);

    public Validation<List<CRUDFailure>, Specification<T>> build(final FilterExpression filterExpression) {
        return validateFilterExpression(filterExpression);
    }

    private Validation<List<CRUDFailure>, Specification<T>> validateFilterExpression(
            final FilterExpression filterExpression) {

        FilterExpression.ExpressionType expressionType = filterExpression.getExpressionType();

        switch (expressionType) {
            case SIMPLE:
                return validateSimpleFilter((Filter) filterExpression);

            case COMBINED:
                return validateCombinedFilter((CombinedFilter) filterExpression);
        }

        throw new UnsupportedOperationException("Unsupported filter expression type " + expressionType);
    }

    private Validation<List<CRUDFailure>, Specification<T>> validateSimpleFilter(final Filter filter) {
        return build(filter).mapError(Collections::singletonList);
    }

    private Validation<List<CRUDFailure>, Specification<T>> validateCombinedFilter(
            final CombinedFilter combinedFilter) {

        return combinedFilter.getFilters().stream()
                .map(this::validateFilterExpression)
                .collect(CollectorUtils.groupCollectionValidations())
                .flatMap(specs -> this.combineSpecifications(specs, combinedFilter.getOperator()));
    }

    private Validation<List<CRUDFailure>, Specification<T>> combineSpecifications(
            final List<Specification<T>> specifications, final BooleanOperator booleanOperator) {

        switch (booleanOperator) {
            case AND:
                return Option.ofOptional(specifications.stream().reduce(Specification::and))
                        .toValid(() -> singletonList(emptyCombinedFilter()));
            case OR:
                return Option.ofOptional(specifications.stream().reduce(Specification::or))
                        .toValid(() -> singletonList(emptyCombinedFilter()));

            default:
                throw new UnsupportedOperationException("Unsupported boolean operator " + booleanOperator);
        }
    }

    private CRUDFailure emptyCombinedFilter() {
        return CRUDFailure.invalidRequestParameter("search",
                "Combined filter expression must have at least one filter");
    }
}
