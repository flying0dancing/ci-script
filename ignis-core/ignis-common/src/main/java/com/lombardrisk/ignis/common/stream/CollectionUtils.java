package com.lombardrisk.ignis.common.stream;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

@UtilityClass
public class CollectionUtils {

    public static <T, C extends Collection<T>> C concat(
            final C head,
            final T tail,
            final Supplier<C> collectionSupplier) {
        C collection = collectionSupplier.get();
        collection.addAll(head);
        collection.add(tail);
        return collection;
    }

    public static <T> List<T> orEmpty(final List<T> list) {
        if (list == null) {
            return emptyList();
        }

        return list;
    }

    public static <T> Set<T> orEmpty(final Set<T> set) {
        if (set == null) {
            return emptySet();
        }

        return set;
    }
}
