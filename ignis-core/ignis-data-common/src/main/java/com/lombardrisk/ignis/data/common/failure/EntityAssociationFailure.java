package com.lombardrisk.ignis.data.common.failure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Data
public class EntityAssociationFailure implements CRUDFailure {

    private final long id;
    private final String entityName;
    private final String associatedEntityName;
    private final List<Long> associatedEntityIds;
    private final CrudOperation crudOperation;

    @Override
    public Type getType() {
        return Type.ENTITY_IS_ASSOCIATED;
    }

    @Override
    public String getErrorMessage() {
        return "Could not " + crudOperation.name() + " " + entityName + " with id " + id
                + " because it has associated " + associatedEntityName + "s with ids " + associatedEntityIds;
    }
}
