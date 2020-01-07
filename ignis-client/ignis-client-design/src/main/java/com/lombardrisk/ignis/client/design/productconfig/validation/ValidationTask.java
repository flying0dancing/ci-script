package com.lombardrisk.ignis.client.design.productconfig.validation;

import java.util.Collections;
import java.util.List;

public interface ValidationTask {

    String getName();

    TaskType getType();

    TaskStatus getStatus();

    String getMessage();

    default List<? extends ValidationTask> getTasks() {
        return Collections.emptyList();
    }
}
