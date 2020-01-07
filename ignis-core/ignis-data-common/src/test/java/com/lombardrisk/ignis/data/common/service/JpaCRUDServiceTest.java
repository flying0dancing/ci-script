package com.lombardrisk.ignis.data.common.service;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.service.JpaCRUDService;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JpaCRUDServiceTest {

    @Mock
    private JpaRepository<StringIdentifiable, Long> repository;

    private JpaCRUDService<StringIdentifiable> service;

    @Before
    public void setUp() {
        service = new TestImpl(repository);
    }

    @Test
    public void findById_CallsRepository() {
        service.findById(1802L);

        verify(repository).findById(1802L);
    }

    @Test
    public void findById_NotFound_ReturnsOptionNone() {
        when(repository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Option<StringIdentifiable> byId = service.findById(1802L);

        assertThat(byId)
                .isEqualTo(Option.none());
    }

    @Test
    public void findById_ObjectFound_ReturnsOptionWithValue() {
        StringIdentifiable stringIdentifiable = new StringIdentifiable(1, "1");

        when(repository.findById(anyLong()))
                .thenReturn(Optional.of(stringIdentifiable));

        Option<StringIdentifiable> byId = service.findById(1802L);

        assertThat(byId)
                .isEqualTo(Option.of(stringIdentifiable));
    }

    @Test
    public void findAll_CallsRepository() {
        service.findAll();
        verify(repository).findAll();
    }

    @Test
    public void findAll_ReturnsListFromRepository() {
        List<StringIdentifiable> objects = Arrays.asList(
                new StringIdentifiable(1802, "1802"),
                new StringIdentifiable(1803, "1803"),
                new StringIdentifiable(1804, "1804")
        );

        when(repository.findAll())
                .thenReturn(objects);

        List<StringIdentifiable> all = service.findAll();
        assertThat(all)
                .isSameAs(objects);
    }

    @Test
    public void findAllById_CallsRepository() {
        List<Long> ids = Arrays.asList(1802L, 1803L, 1804L);
        service.findAllByIds(ids);

        verify(repository).findAllById(ids);
    }

    @Test
    public void findAllById_ReturnsListFromRepository() {
        List<StringIdentifiable> objects = Arrays.asList(
                new StringIdentifiable(1802, "1802"),
                new StringIdentifiable(1803, "1803"),
                new StringIdentifiable(1804, "1804")
        );

        when(repository.findAllById(any()))
                .thenReturn(objects);

        List<StringIdentifiable> allByIds = service.findAllByIds(Arrays.asList(1802L, 1803L, 1804L));
        assertThat(allByIds)
                .isSameAs(objects);
    }

    @Test
    public void delete_CallsRepository() {
        StringIdentifiable stringIdentifiable = new StringIdentifiable(1, "1");

        service.delete(stringIdentifiable);

        verify(repository).delete(stringIdentifiable);
    }

    @Test
    public void delete_ReturnsDeletedObject() {
        StringIdentifiable stringIdentifiable = new StringIdentifiable(1, "1");

        StringIdentifiable deleted = service.delete(stringIdentifiable);

        assertThat(stringIdentifiable)
                .isSameAs(deleted);
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

    public static class TestImpl implements JpaCRUDService<StringIdentifiable> {

        private final JpaRepository<StringIdentifiable, Long> repository;

        public TestImpl(final JpaRepository<StringIdentifiable, Long> repository) {
            this.repository = repository;
        }

        @Override
        public String entityName() {
            return "StringObj";
        }

        @Override
        public JpaRepository<StringIdentifiable, Long> repository() {
            return repository;
        }
    }
}
