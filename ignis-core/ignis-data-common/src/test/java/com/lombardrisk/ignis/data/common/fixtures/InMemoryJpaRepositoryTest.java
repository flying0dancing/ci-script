package com.lombardrisk.ignis.data.common.fixtures;

import com.lombardrisk.ignis.data.common.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryJpaRepositoryTest {

    @Test
    public void save_IdNotPresent_GeneratesNewId() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();

        TestEntity entity1 = new TestEntity(null);
        TestEntity entity2 = new TestEntity(null);
        TestEntity entity3 = new TestEntity(null);

        repository.saveAll(Arrays.asList(entity1, entity2, entity3));

        List<Long> ids = repository.findAll().stream()
                .map(TestEntity::getId)
                .collect(Collectors.toList());

        assertThat(ids)
                .contains(1L, 2L, 3L);
    }

    @Test
    public void save_IdPresent_DoesNotGenerate() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();

        TestEntity entity1 = new TestEntity(82L);
        TestEntity entity2 = new TestEntity(83L);
        TestEntity entity3 = new TestEntity(84L);

        repository.saveAll(Arrays.asList(entity1, entity2, entity3));

        List<Long> ids = repository.findAll().stream()
                .map(TestEntity::getId)
                .collect(Collectors.toList());

        assertThat(ids)
                .contains(82L, 83L, 84L);
    }

    @Test
    public void findById_EntityPresent_ReturnsEntity() {

        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();
        TestEntity entity = new TestEntity(1L);
        repository.save(entity);

        Optional<TestEntity> find = repository.findById(1L);
        assertThat(find)
                .hasValue(entity);
    }

    @Test
    public void findById_EntityNotPresent_ReturnsEmpty() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();

        Optional<TestEntity> find = repository.findById(12312L);
        assertThat(find)
                .isEmpty();
    }

    @Test
    public void findByIds_SomeEntityPresent_ReturnsEntities() {

        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();
        TestEntity entity1 = new TestEntity(1L);
        repository.save(entity1);
        TestEntity entity2 = new TestEntity(2L);
        repository.save(entity2);
        TestEntity entity3 = new TestEntity(3L);
        repository.save(entity3);
        TestEntity entity4 = new TestEntity(4L);
        repository.save(entity4);

        List<TestEntity> find = repository.findAllByIds(Arrays.asList(1L, 2L, 3L, 5L));

        assertThat(find).contains(entity1, entity2, entity3);
    }

    @Test
    public void saveAll_SavesAllToRepo() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();
        TestEntity entity1 = new TestEntity(1L);
        repository.save(entity1);
        TestEntity entity2 = new TestEntity(2L);
        repository.save(entity2);
        TestEntity entity3 = new TestEntity(3L);
        repository.save(entity3);
        TestEntity entity4 = new TestEntity(4L);
        repository.save(entity4);

        repository.saveAll(Arrays.asList(entity1, entity2, entity3, entity4));

        List<TestEntity> all = repository.findAll();

        assertThat(all)
                .contains(entity1, entity2, entity3, entity4);
    }

    @Test
    public void delete_RemovesEntity() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();
        TestEntity entity1 = new TestEntity(1L);
        repository.save(entity1);
        TestEntity entity2 = new TestEntity(2L);
        repository.save(entity2);

        repository.saveAll(Arrays.asList(entity1, entity2));

        repository.delete(entity1);

        Optional<TestEntity> byId = repository.findById(1L);
        assertThat(byId).isEmpty();
    }

    @Test
    public void deleteById_RemovesEntity() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();
        TestEntity entity1 = new TestEntity(1L);
        repository.save(entity1);

        repository.deleteById(1L);

        Optional<TestEntity> byId = repository.findById(1L);
        assertThat(byId).isEmpty();
    }

    @Test
    public void deleteAll_RemovesEntities() {
        InMemoryRepository<TestEntity> repository = InMemoryRepository.emptyRepository();
        TestEntity entity1 = new TestEntity(1L);
        TestEntity entity2 = new TestEntity(2L);

        repository.saveAll(Arrays.asList(entity1, entity2));

        repository.deleteAll(Arrays.asList(entity1, entity2));

        List<TestEntity> all = repository.findAll();

        assertThat(all).isEmpty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestEntity implements Identifiable {

        private Long id;
    }
}
