package com.lombardrisk.ignis.server.dataset.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDate;

@Builder
@Data
public class DatasetQuery {

    private final String name;
    private final String schema;
    @NonNull
    private final String entityCode;
    @NonNull
    private final LocalDate referenceDate;
}
