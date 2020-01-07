package com.lombardrisk.ignis.data.common.service;

import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import io.vavr.control.Validation;

public interface CRUDService<T extends Identifiable> extends RetrieveService<T> {

    T delete(T t);

    default Validation<CRUDFailure, T> deleteWithValidation(final long id) {
        return findWithValidation(id)
                .map(this::delete);
    }
}
