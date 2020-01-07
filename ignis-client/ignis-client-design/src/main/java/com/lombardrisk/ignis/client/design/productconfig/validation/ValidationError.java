package com.lombardrisk.ignis.client.design.productconfig.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ValidationError implements ValidationTask {

    private final String name;
    private final String message;

    @Override
    public TaskType getType() {
        return TaskType.ERROR;
    }

    @Override
    public TaskStatus getStatus() {
        return TaskStatus.FAILED;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
