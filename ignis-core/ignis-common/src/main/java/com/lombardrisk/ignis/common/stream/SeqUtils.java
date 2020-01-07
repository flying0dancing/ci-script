package com.lombardrisk.ignis.common.stream;

import io.vavr.collection.Seq;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@UtilityClass
public class SeqUtils {

    public static <T> List<T> flatMapToList(final Seq<? extends Collection<T>> seqOfLists) {
        return seqOfLists.toJavaList().stream().flatMap(Collection::stream).collect(toList());
    }

    public static boolean isNullOrEmpty(final Collection<?> toCheck) {
        return toCheck == null || toCheck.isEmpty();
    }

    public static boolean isNullOrEmpty(final Map<?, ?> toCheck) {
        return toCheck == null || toCheck.isEmpty();
    }
}
