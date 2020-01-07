package com.lombardrisk.ignis.data.common.fixtures;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.data.common.Identifiable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("NullableProblems")
public class InMemoryRepository<T extends Identifiable> {

    private final AtomicLong idSequence = new AtomicLong(0);
    private final Map<Long, T> indexLookup = new HashMap<>();

    @VisibleForTesting
    static <V extends Identifiable> InMemoryRepository<V> emptyRepository() {
        return new InMemoryRepository<>();
    }

    public List<T> findAll() {
        return new ArrayList<>(indexLookup.values());
    }

    public List<T> findAllByIds(final Iterable<Long> ids) {
        List<Long> idCollection = ImmutableList.copyOf(ids);
        return indexLookup.entrySet()
                .stream()
                .filter(entry -> idCollection.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public long count() {
        return indexLookup.size();
    }

    public void deleteById(final Long id) {
        indexLookup.remove(id);
    }

    public void delete(final T entity) {
        Optional<Long> found = indexLookup.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(entity))
                .map(Map.Entry::getKey)
                .findFirst();

        found.ifPresent(indexLookup::remove);
    }

    public void deleteAll(final Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    public void deleteAll() {
        indexLookup.clear();
    }

    public <S extends T> S save(final S entity) {
        Objects.requireNonNull(entity);

        if (entity.getId() == null) {
            Long newId = idSequence.incrementAndGet();

            entity.setId(newId);
            indexLookup.put(newId, entity);
            return entity;
        }

        indexLookup.put(entity.getId(), entity);
        return entity;
    }

    public <S extends T> List<S> saveAll(final Iterable<S> entities) {
        return ImmutableList.copyOf(entities)
                .stream()
                .map(this::save)
                .collect(Collectors.toList());
    }

    public Optional<T> findById(final Long id) {
        return Optional.ofNullable(indexLookup.get(id));
    }

    public boolean existsById(final Long id) {
        return indexLookup.containsKey(id);
    }

    private void put(final long key, final T value) {
        indexLookup.put(key, value);
    }

    private Stream<Long> getIds() {
        return indexLookup.entrySet()
                .stream()
                .map(Map.Entry::getKey);
    }
}
