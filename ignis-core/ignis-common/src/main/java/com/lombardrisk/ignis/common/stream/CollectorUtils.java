package com.lombardrisk.ignis.common.stream;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;

@UtilityClass
public final class CollectorUtils {

    public static <T, V, R> Collector<T, Map<R, Set<V>>, Map<R, Set<V>>> toMultimap(
            final Function<T, R> keyExtractor,
            final Function<T, V> valueMapper) {
        return Collector.of(
                HashMap::new,
                (map, t) -> accumulateToMultiMap(keyExtractor, valueMapper, map, t),
                CollectorUtils::combineMultiMaps,
                map -> map);
    }

    public static <T, V, R> Collector<T, Map<R, List<V>>, Map<R, List<V>>> toMultimapList(
            final Function<T, R> keyExtractor,
            final Function<T, V> valueMapper) {
        return Collector.of(
                HashMap::new,
                (map, t) -> accumulateToMultiMapList(keyExtractor, valueMapper, map, t),
                CollectorUtils::combineMultiMapsList,
                map -> map);
    }

    public static <T, K1, K2, V1, V2> Collector<T, Map<K1, Map<K2, V2>>, Map<K1, Map<K2, V2>>> toNestedMap(
            final Function<T, K1> keyExtractor,
            final Function<T, V1> valueMapper,
            final Function<V1, K2> valueKeyExtractor,
            final Function<V1, V2> valueValueExtractor) {

        return Collector.of(
                HashMap::new,
                (map, t) -> accumulateToNestedMap(
                        keyExtractor, valueMapper, valueKeyExtractor, valueValueExtractor, map, t),
                (k1MapMap, k1MapMap2) -> notSupported(),
                map -> map);
    }

    private static <K1, K2, V1, V2> Map<K1, Map<K2, V2>> notSupported() {
        throw new NotImplementedException("Parallel streams not supported for nested map collection");
    }

    public static <T, R> Collector<T, EntitiesAndProperty<T, R>, EntitiesAndProperty<T, R>> extractProperty(
            final Function<T, R> propertyExtractor) {
        return Collector.of(
                EntitiesAndProperty::mutableEmpty,
                (a, entity) -> EntitiesAndProperty.accumulate(propertyExtractor, a, entity),
                EntitiesAndProperty::combine,
                EntitiesAndProperty::finish);
    }

    public static <T, R> Collector<T, EntitiesAndProperty<T, R>, EntitiesAndProperty<T, R>> extractPropertyCollection(
            final Function<T, List<R>> propertyExtractor) {
        return Collector.of(
                EntitiesAndProperty::mutableEmpty,
                (a, entity) -> EntitiesAndProperty.accumulateList(propertyExtractor, a, entity),
                EntitiesAndProperty::combine,
                EntitiesAndProperty::finish);
    }

    public static <E, P> Collector<Validation<E, P>, Tuple2<List<E>, List<P>>, Validation<List<E>, List<P>>> groupValidations() {
        return Collector.of(
                () -> Tuple.of(new ArrayList<>(), new ArrayList<>()),
                CollectorUtils::accumulateValidations,
                CollectorUtils::combineValidations,
                CollectorUtils::finishByGroupingErrors);
    }

    public static <E, C extends Collection<E>, P> Collector<
            Validation<C, P>,
            Tuple2<List<E>, List<P>>,
            Validation<List<E>, List<P>>> groupCollectionValidations() {

        return Collector.of(
                () -> Tuple.of(new ArrayList<>(), new ArrayList<>()),
                CollectorUtils::accumulateCollectionValidations,
                CollectorUtils::combineValidations,
                CollectorUtils::finishByGroupingErrors);
    }

    public static <T> Collector<T, ValuesAndDuplicates<T>, Set<T>> collectDuplicates() {
        return Collector.of(
                ValuesAndDuplicates::new,
                ValuesAndDuplicates::accumulate,
                ValuesAndDuplicates::combine,
                ValuesAndDuplicates::getDuplicates);
    }

    private static <T, R, V> Map<R, Set<V>> accumulateToMultiMap(
            final Function<T, R> keyExtractor,
            final Function<T, V> valueMapper,
            final Map<R, Set<V>> map,
            final T t) {
        R key = keyExtractor.apply(t);
        V value = valueMapper.apply(t);
        map.putIfAbsent(key, new LinkedHashSet<>());
        map.get(key).add(value);
        return map;
    }

    private static <T, R, V> Map<R, List<V>> accumulateToMultiMapList(
            final Function<T, R> keyExtractor,
            final Function<T, V> valueMapper,
            final Map<R, List<V>> map,
            final T t) {
        R key = keyExtractor.apply(t);
        V value = valueMapper.apply(t);
        map.putIfAbsent(key, new ArrayList<>());
        map.get(key).add(value);
        return map;
    }

    private static <T, V1, V2, K1, K2> Map<K1, Map<K2, V2>> accumulateToNestedMap(
            final Function<T, K1> keyExtractor,
            final Function<T, V1> valueMapper,
            final Function<V1, K2> valueKeyExtractor,
            final Function<V1, V2> valueValueExtractor,
            final Map<K1, Map<K2, V2>> map,
            final T t) {

        K1 key1 = keyExtractor.apply(t);
        V1 value = valueMapper.apply(t);
        K2 key2 = valueKeyExtractor.apply(value);
        V2 value2 = valueValueExtractor.apply(value);

        map.putIfAbsent(key1, new HashMap<>());

        Map<K2, V2> firstNestedMap = map.get(key1);
        firstNestedMap.put(key2, value2);
        return map;
    }

    private static <T, R> Map<R, Set<T>> combineMultiMaps(final Map<R, Set<T>> map1, final Map<R, Set<T>> map2) {
        map2.forEach((key, ts) -> {
            map1.computeIfPresent(key, (r, existingList) -> {
                existingList.addAll(ts);
                return existingList;
            });
            map1.putIfAbsent(key, ts);
        });
        return map1;
    }

    private static <T, R> Map<R, List<T>> combineMultiMapsList(final Map<R, List<T>> map1, final Map<R, List<T>> map2) {
        map2.forEach((key, ts) -> {
            map1.computeIfPresent(key, (r, existingList) -> {
                existingList.addAll(ts);
                return existingList;
            });
            map1.putIfAbsent(key, ts);
        });
        return map1;
    }

    private static <E, P> Tuple2<List<E>, List<P>> combineValidations(
            final Tuple2<List<E>, List<P>> toRetain,
            final Tuple2<List<E>, List<P>> throwAway) {
        toRetain._2.addAll(throwAway._2);
        toRetain._1.addAll(throwAway._1);
        return toRetain;
    }

    private static <E, P> void accumulateValidations(
            final Tuple2<List<E>, List<P>> tuple,
            final Validation<E, P> validation) {
        if (validation.isInvalid()) {
            tuple._1.add(validation.getError());
        } else {
            tuple._2.add(validation.get());
        }
    }

    private static <E, C extends Collection<E>, P> void accumulateCollectionValidations(
            final Tuple2<List<E>, List<P>> tuple,
            final Validation<C, P> validation) {
        if (validation.isInvalid()) {
            tuple._1.addAll(validation.getError());
        } else {
            tuple._2.add(validation.get());
        }
    }

    private static <E, P> Validation<List<E>, List<P>> finishByGroupingErrors(final Tuple2<List<E>, List<P>> tuple) {
        if (tuple._1().isEmpty()) {
            return Validation.valid(tuple._2());
        } else {
            return Validation.invalid(tuple._1());
        }
    }
}
