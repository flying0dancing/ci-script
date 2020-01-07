package com.lombardrisk.ignis.data.common.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.RetrieveService;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RetrieveServiceTest {

    private final RetrieveService<StringIdentifiable> service =
            new TestImpl(Collections.singletonList(new StringIdentifiable(100, "SomeString")));

    @Test
    public void findWithValidation_ObjectNotFound_ReturnsCRUDFailure() {
        assertThat(service.findWithValidation(-100).getError())
                .isEqualTo(CRUDFailure.notFoundIds("StringObj", -100L));
    }

    @Test
    public void findWithValidation_ObjectFound_ReturnsObject() {
        assertThat(service.findWithValidation(100).get().getValue())
                .isEqualTo("SomeString");
    }

    @Test
    public void findTablesOrIdsNotFound_SomeObjectsNotFound_ReturnsFailure() {
        RetrieveService<StringIdentifiable> service = new TestImpl(
                Arrays.asList(
                        new StringIdentifiable(1L, "1"),
                        new StringIdentifiable(3L, "3")));

        assertThat(service.findOrIdsNotFound(ImmutableSet.of(1L, 2L, 3L, 4L)).getError())
                .isEqualTo(CRUDFailure.notFoundIds("StringObj", 2L, 4L));
    }

    @Test
    public void findTablesOrIdsNotFound_AllObjectsFound_ReturnsObjects() {
        List<StringIdentifiable> objects = Arrays.asList(
                new StringIdentifiable(1L, "1"),
                new StringIdentifiable(2L, "3"),
                new StringIdentifiable(3L, "3"),
                new StringIdentifiable(4L, "4"));

        RetrieveService<StringIdentifiable> service = new TestImpl(objects);

        assertThat(service.findOrIdsNotFound(ImmutableSet.of(1L, 2L, 3L, 4L)).get())
                .isEqualTo(objects);
    }

    @AllArgsConstructor
    public static class StringIdentifiable implements Identifiable {

        private final long id;

        @Getter
        private final String value;

        @Override
        public Long getId() {
            return id;
        }
    }

    public static class TestImpl implements RetrieveService<StringIdentifiable> {

        private final List<StringIdentifiable> objects;

        public TestImpl(final List<StringIdentifiable> objects) {
            this.objects = objects;
        }

        @Override
        public String entityName() {
            return "StringObj";
        }

        @Override
        public Option<StringIdentifiable> findById(final long id) {
            Optional<StringIdentifiable> identifiable = objects.stream()
                    .filter(stringId -> stringId.getId() == id)
                    .findFirst();

            return Option.ofOptional(identifiable);
        }

        @Override
        public List<StringIdentifiable> findAllByIds(final Iterable<Long> ids) {
            ImmutableList<Long> idsList = ImmutableList.copyOf(ids);

            return objects.stream()
                    .filter(stringIdentifiable -> idsList.contains(stringIdentifiable.getId()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<StringIdentifiable> findAll() {
            return objects;
        }
    }
}
