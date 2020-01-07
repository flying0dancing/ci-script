package com.lombardrisk.ignis.data.common.failure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
public class NotFoundFailure<T> implements CRUDFailure {

    private final String indexName;
    private final String name;
    private final List<T> ids;

    @Override
    public Type getType() {
        return Type.NOT_FOUND;
    }

    @Override
    public String getErrorMessage() {
        return "Could not find " + name + " for " + indexName + "s " + ids;
    }
}
