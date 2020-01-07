package com.lombardrisk.ignis.data.common.service;

import com.lombardrisk.ignis.data.common.Identifiable;
import io.vavr.control.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCRUDService<T extends Identifiable> extends CRUDService<T> {

    JpaRepository<T, Long> repository();

    default Option<T> findById(final long id) {
        return Option.ofOptional(repository().findById(id));
    }

    default List<T> findAll() {
        return repository().findAll();
    }

    default List<T> findAllByIds(final Iterable<Long> ids) {
        return repository().findAllById(ids);
    }

    default T delete(final T t) {
        repository().delete(t);
        return t;
    }
}
