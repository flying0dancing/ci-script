package com.lombardrisk.ignis.common.stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class EntitiesAndProperty<ENTITY, PROPERTY> {

    private final Set<PROPERTY> propertyValues;
    private final List<ENTITY> entities;

    static <T, R> EntitiesAndProperty<T, R> mutableEmpty() {
        return new EntitiesAndProperty<>(new LinkedHashSet<>(), new ArrayList<>());
    }

    static <T, R> EntitiesAndProperty<T, R> combine(
            final EntitiesAndProperty<T, R> retained,
            final EntitiesAndProperty<T, R> throwAway) {
        retained.propertyValues.addAll(throwAway.propertyValues);
        retained.entities.addAll(throwAway.entities);

        return retained;
    }

    static <T, R> EntitiesAndProperty<T, R> accumulate(
            final Function<T, R> propertyExtractor,
            final EntitiesAndProperty<T, R> retained,
            final T entity) {
        retained.propertyValues.add(propertyExtractor.apply(entity));
        retained.entities.add(entity);

        return retained;
    }

    static <T, R> EntitiesAndProperty<T, R> accumulateList(
            final Function<T, List<R>> propertyExtractor,
            final EntitiesAndProperty<T, R> retained,
            final T entity) {
        retained.propertyValues.addAll(propertyExtractor.apply(entity));
        retained.entities.add(entity);

        return retained;
    }

    static <T, R> EntitiesAndProperty<T, R> finish(
            final EntitiesAndProperty<T, R> toFinish) {
        return new EntitiesAndProperty<>(
                ImmutableSet.copyOf(toFinish.propertyValues), ImmutableList.copyOf(toFinish.entities));
    }
}
