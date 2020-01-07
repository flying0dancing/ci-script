package com.lombardrisk.ignis.common.stream;

import lombok.Value;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Value
class ValuesAndDuplicates<T> {

    private final List<T> values = new ArrayList<>();
    private final Set<T> duplicates = new LinkedHashSet<>();

    static <R> void accumulate(final ValuesAndDuplicates<R> valuesAndDuplicates, final R value) {
        if (valuesAndDuplicates.values.contains(value)) {
            valuesAndDuplicates.duplicates.add(value);
        }

        valuesAndDuplicates.values.add(value);
    }

    static <R> ValuesAndDuplicates<R> combine(
            final ValuesAndDuplicates<R> retain, final ValuesAndDuplicates<R> merge) {

        retain.values.addAll(merge.values);
        retain.duplicates.addAll(merge.duplicates);

        return retain;
    }
}
