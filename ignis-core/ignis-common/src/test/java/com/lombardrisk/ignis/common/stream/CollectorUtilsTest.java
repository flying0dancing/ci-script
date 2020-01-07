package com.lombardrisk.ignis.common.stream;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class CollectorUtilsTest {

    @Test
    public void toMultiMap_ExtractsValuesInToMapAndList() {
        TestEntity hob = new TestEntity("HOB", "KITCHEN APPLIANCE");
        TestEntity blob = new TestEntity("BLOB", "MONSTER");
        TestEntity drBlob = new TestEntity("BLOB", "ORTHOPAEDIC SURGEON");

        Map<String, Set<String>> result = Stream.of(blob, drBlob, hob)
                .collect(CollectorUtils.toMultimap(TestEntity::getName, TestEntity::getOccupation));

        assertThat(result)
                .contains(
                        entry("BLOB", newHashSet("MONSTER", "ORTHOPAEDIC SURGEON")),
                        entry("HOB", singleton("KITCHEN APPLIANCE")));
    }

    @Test
    public void toNestedMap_ExtractsValuesInToMapAndList() {
        Hierarchy groundFish = Hierarchy.bottomFeeder("Groundfish");
        Hierarchy goldFish = new Hierarchy("Goldfish", groundFish);

        Hierarchy shark = new Hierarchy("Shark", goldFish);

        Map<String, Map<String, String>> result = Stream.of(shark, goldFish, groundFish)
                .collect(CollectorUtils.toNestedMap(
                        Hierarchy::getName,
                        Hierarchy::getUnderling,
                        hierarchy -> Optional.ofNullable(hierarchy)
                                .map(Hierarchy::getName)
                                .orElse(null),
                        hierarchy -> Optional.ofNullable(hierarchy)
                                .flatMap(h -> Optional.ofNullable(h.underling))
                                .map(Hierarchy::getName)
                                .orElse(null)));

        assertThat(result)
                .isEqualTo(new HashMap<String, Map<String, String>>() {
                    private static final long serialVersionUID = 5168497648312552777L;
                    {
                        put("Shark", singletonMap("Goldfish", "Groundfish"));
                        put("Goldfish", singletonMap("Groundfish", null));
                        put("Groundfish", singletonMap(null, null));
                    }
                });
    }

    @Test
    public void extractProperty_ReturnsPropertiesAndEntities() {
        EntitiesAndProperty<TestEntity, String> result = Stream.of(
                new TestEntity("BLOB", "MONSTER"), new TestEntity("HOB", "KITCHEN APPLIANCE"))
                .collect(CollectorUtils.extractProperty(TestEntity::getName));

        assertThat(result.getPropertyValues())
                .containsExactlyInAnyOrder("BLOB", "HOB");

        assertThat(result.getEntities())
                .containsExactly(new TestEntity("BLOB", "MONSTER"), new TestEntity("HOB", "KITCHEN APPLIANCE"));
    }

    @Test
    public void extractPropertyCollection_ReturnsPropertiesAndEntities() {
        EntitiesAndProperty<FacedEntity, String> godsAndTheirFaces = Stream.of(
                new FacedEntity(
                        "The Many Faced God",
                        Arrays.asList("Stranger", "The Pale Child", "The Merling King", "The Black Goat of Qohor")),
                new FacedEntity(
                        "The Father",
                        singletonList("The Father")))
                .collect(CollectorUtils.extractPropertyCollection(FacedEntity::getFaces));

        assertThat(godsAndTheirFaces.getPropertyValues())
                .containsExactlyInAnyOrder(
                        "Stranger",
                        "The Pale Child",
                        "The Merling King",
                        "The Black Goat of Qohor",
                        "The Father");

        assertThat(godsAndTheirFaces.getEntities())
                .containsExactly(
                        new FacedEntity(
                                "The Many Faced God",
                                Arrays.asList(
                                        "Stranger",
                                        "The Pale Child",
                                        "The Merling King",
                                        "The Black Goat of Qohor")),
                        new FacedEntity("The Father", singletonList("The Father")));
    }

    @AllArgsConstructor
    @Data
    public static class TestEntity {

        private final String name;
        private final String occupation;
    }

    @AllArgsConstructor
    @Data
    public static class FacedEntity {

        private final String name;
        private final List<String> faces;
    }

    @AllArgsConstructor
    @Data
    public static class Hierarchy {

        private final String name;
        private final Hierarchy underling;

        public static Hierarchy bottomFeeder(final String name) {
            return new Hierarchy(name, null);
        }
    }

    @Test
    public void groupValidations_SomeErrors_ReturnsInvalid() {
        List<Validation<String, Double>> validations = ImmutableList.of(
                Validation.valid(23.0),
                Validation.invalid("NAH"),
                Validation.invalid("MATE"),
                Validation.valid(99.0)
        );

        Validation<List<String>, List<Double>> result = validations.stream()
                .collect(CollectorUtils.groupValidations());

        VavrAssert.assertFailed(result)
                .withFailure(Arrays.asList("NAH", "MATE"));
    }

    @Test
    public void groupValidations_AllElementsValid_ReturnsValid() {
        List<Validation<String, Double>> validations = ImmutableList.of(
                Validation.valid(23.0),
                Validation.valid(42.0),
                Validation.valid(63.0),
                Validation.valid(99.0)
        );

        Validation<List<String>, List<Double>> result = validations.stream()
                .collect(CollectorUtils.groupValidations());

        VavrAssert.assertValid(result)
                .withResult(Arrays.asList(23.0, 42.0, 63.0, 99.0));
    }

    @Test
    public void groupCollectionValidations_SomeErrors_ReturnsInvalid() {
        List<Validation<List<String>, Double>> validations = ImmutableList.of(
                Validation.valid(23.0),
                Validation.invalid(Arrays.asList("NAH", "MATE")),
                Validation.invalid(Arrays.asList("NOT", "TODAY")),
                Validation.valid(99.0)
        );

        Validation<List<String>, List<Double>> result = validations.stream()
                .collect(CollectorUtils.groupCollectionValidations());

        VavrAssert.assertFailed(result)
                .withFailure(Arrays.asList("NAH", "MATE", "NOT", "TODAY"));
    }

    @Test
    public void groupCollectionValidations_AllElementsValid_ReturnsValid() {
        List<Validation<List<String>, Double>> validations = ImmutableList.of(
                Validation.valid(23.0),
                Validation.valid(42.0),
                Validation.valid(63.0),
                Validation.valid(99.0)
        );

        Validation<List<String>, List<Double>> result = validations.stream()
                .collect(CollectorUtils.groupCollectionValidations());

        VavrAssert.assertValid(result)
                .withResult(Arrays.asList(23.0, 42.0, 63.0, 99.0));
    }

    @Test
    public void collectDuplicates_SomeDuplicates_ReturnsSetOfDuplicates() {
        List<String> values = ImmutableList.of(
                "A", "B", "C", "D", "E", "F", "A", "B", "C", "A", "B", "C"
        );

        Set<String> duplicates = values.stream()
                .collect(CollectorUtils.collectDuplicates());

        assertThat(duplicates)
                .containsOnly("A", "B", "C");
    }

    @Test
    public void collectDuplicates_NoDuplicates_ReturnsEmptySet() {
        List<String> values = ImmutableList.of("A", "B", "C", "D", "E", "F");

        Set<String> duplicates = values.stream()
                .collect(CollectorUtils.collectDuplicates());

        assertThat(duplicates).isEmpty();
    }
}