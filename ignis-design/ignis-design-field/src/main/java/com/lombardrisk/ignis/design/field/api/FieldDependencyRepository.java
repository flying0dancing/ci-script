package com.lombardrisk.ignis.design.field.api;

import com.lombardrisk.ignis.design.field.model.Field;

public interface FieldDependencyRepository {

    void deleteForField(Field field);
}
