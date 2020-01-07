package com.lombardrisk.ignis.common;

import com.google.common.collect.ImmutableList;
import io.vavr.Function1;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.lombardrisk.ignis.common.MapperUtils.map;
import static com.lombardrisk.ignis.common.MapperUtils.mapSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

public class MapperUtilsTest {

    @Test
    public void map_AppliesFunctionToCollection() {
        List<String> originalNames = asList("Bob", "Garfield", "Dr. Eggman");

        Function1<String, String> nameUpperFunction = (Function1<String, String>) String::toUpperCase;

        List<String> upperCasedNames = map(originalNames, nameUpperFunction);

        assertThat(upperCasedNames).containsExactly("BOB", "GARFIELD", "DR. EGGMAN");
    }

    @Test
    public void mapSet_ReturnsSetWithFunctionApplied() {
        List<String> originalNames = asList("Bob", "Garfield", "Dr. Eggman", "Dr. Eggman");

        Set<String> upperCasedNames = mapSet(originalNames, String::toUpperCase);

        assertThat(upperCasedNames)
                .containsExactlyInAnyOrder("BOB", "GARFIELD", "DR. EGGMAN");
    }

    @Test
    public void mapIndexedList_AddIndexToStrings_ReturnsStringsWithIndex() {
        List<String> originalNames = asList("Bob", "Garfield", "Dr. Eggman");

        List<String> indexedStrings = MapperUtils.mapListWithIndex(originalNames, (i, string) -> i + string);

        assertThat(indexedStrings)
                .containsExactly(
                        "0Bob",
                        "1Garfield",
                        "2Dr. Eggman");
    }

    @Test
    public void mapListToIndexedMap_EmptyList_ReturnsEmptyMap() {
        Map<Integer, String> map = MapperUtils.mapListToIndexedMap(new ArrayList<>());

        assertThat(map).isEmpty();
    }

    @Test
    public void mapListToIndexedMap_PopulatedList_ReturnsMap() {
        List<String> strings = ImmutableList.of(
                "0",
                "1",
                "2",
                "3",
                "4"
        );

        Map<Integer, String> map = MapperUtils.mapListToIndexedMap(strings);

        assertThat(map).contains(
                entry(0, "0"),
                entry(1, "1"),
                entry(2, "2"),
                entry(3, "3"),
                entry(4, "4"));
    }

    @Test
    public void mapOrEmpty_Null_ReturnsEmpty() {
        List<String> strings = MapperUtils.mapOrEmpty(null, Object::toString);

        assertThat(strings).isNotNull();
        assertThat(strings).isEmpty();
    }

    @Test
    public void mapOrEmpty_List_ReturnsList() {
        List<String> strings = MapperUtils.mapOrEmpty(singleton(1), Object::toString);

        assertThat(strings).contains("1");
    }

    @Test
    public void mapOrEmptySet_Null_ReturnsEmpty() {
        Set<String> strings = MapperUtils.mapOrEmptySet(null, Object::toString);

        assertThat(strings).isNotNull();
        assertThat(strings).isEmpty();
    }

    @Test
    public void mapOrEmptySet_List_ReturnsList() {
        Set<String> strings = MapperUtils.mapOrEmptySet(singleton(1), Object::toString);

        assertThat(strings).contains("1");
    }

    @Test
    public void mapCollectionOrEmpty_Null_ReturnsEmpty() {
        Set<String> strings = MapperUtils.mapCollectionOrEmpty(null, Object::toString, LinkedHashSet::new);

        assertThat(strings).isNotNull();
        assertThat(strings).isEmpty();
    }

    @Test
    public void mapCollectionOrEmpty_List_ReturnsList() {
        Set<String> strings = MapperUtils.mapCollectionOrEmpty(singleton(1), Object::toString, LinkedHashSet::new);

        assertThat(strings).contains("1");
    }
}