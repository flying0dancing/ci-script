package com.lombardrisk.ignis.server.job.staging.model;

import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * Class to add schema and parsed local date to staging item request after validation.
 * Staging item request is validated and then combined with the schema and reference date to form an instruction.
 */
@Data
@Builder
@AllArgsConstructor
public class StagingDatasetInstruction {

    private final Table schema;
    private final String entityCode;
    private final LocalDate referenceDate;
    private final Dataset appendToDataset;

    private final String filePath;
    private final boolean header;
    private final boolean autoValidate;
}
