package com.lombardrisk.ignis.data.common.service;

import com.google.common.collect.Sets;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import io.vavr.control.Option;
import io.vavr.control.Validation;

import java.util.List;
import java.util.Set;

public interface RetrieveService<T extends Identifiable> {

    String entityName();

    Option<T> findById(long id);

    List<T> findAllByIds(Iterable<Long> ids);
    List<T> findAll();

    default Validation<CRUDFailure, T> findWithValidation(final long id) {
        return findById(id)
                .toValidation(notFound(id));
    }

    default NotFoundFailure notFound(final long id) {
        return CRUDFailure.notFoundIds(entityName(), id);
    }

    default Validation<CRUDFailure, List<T>> findOrIdsNotFound(final Set<Long> ids) {
        List<T> entities = findAllByIds(ids);

        Set<Long> foundTableIds = MapperUtils.mapSet(entities, Identifiable::getId);
        Set<Long> idsNotFound = Sets.difference(ids, foundTableIds).immutableCopy();

        return idsNotFound.isEmpty()
                ? Validation.valid(entities)
                : Validation.invalid(CRUDFailure.notFoundIds(entityName(), idsNotFound));
    }
}
