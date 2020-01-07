package com.lombardrisk.ignis.spark.api.staging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class StagingErrorOutput {

    private String errorFileSystemUri;
    private String temporaryFilePath;

    private String errorFilePath;

}
