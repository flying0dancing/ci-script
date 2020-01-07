package com.lombardrisk.ignis.common;

import io.vavr.Function1;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

@UtilityClass
public final class MapperUtils {

    public static <T, R> List<R> map(final Collection<T> in, final Function1<T, R> mapper) {
        return mapCollection(in, mapper, ArrayList::new);
    }

    public static <T, R> Set<R> mapSet(final Collection<T> in, final Function1<T, R> mapper) {
        return mapCollection(in, mapper, HashSet::new);
    }

    public static <T, R, C extends Collection<R>> C mapCollection(
            final Collection<T> in,
            final Function1<T, R> mapper,
            final Supplier<C> collectionSupplier) {

        return in.stream()
                .map(mapper)
                .collect(toCollection(collectionSupplier));
    }

    public static <T, R, C extends Collection<R>> C mapCollectionOrEmpty(
            final Collection<T> in,
            final Function1<T, R> mapper,
            final Supplier<C> collectionSupplier) {

        return in == null
                ? collectionSupplier.get()
                : mapCollection(in, mapper, collectionSupplier);
    }

    public static <T, R> List<R> mapListWithIndex(
            final List<T> input,
            final BiFunction<Integer, T, R> indexAndValueMapper) {
        List<R> output = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            R functionResult = indexAndValueMapper.apply(i, input.get(i));
            output.add(functionResult);
        }

        return output;
    }

    public static <T> Map<Integer, T> mapListToIndexedMap(final List<T> values) {
        return IntStream.range(0, values.size())
                .boxed()
                .collect(toMap(i -> i, values::get));
    }

    public static <T, R> List<R> mapOrEmpty(final Collection<T> in, final Function1<T, R> converter) {
        return mapCollectionOrEmpty(in, converter, ArrayList::new);
    }

    public static <T, R> Set<R> mapOrEmptySet(final Collection<T> in, final Function1<T, R> converter) {
        return mapCollectionOrEmpty(in, converter, HashSet::new);
    }
}
