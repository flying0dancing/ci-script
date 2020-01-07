package com.lombardrisk.ignis.client.external.job.staging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@JsonIgnoreProperties(ignoreUnknown = true)
public class StagingItemView {

    private final Long id;
    private final Date startTime;
    private final Date endTime;
    private final String message;
    private final String stagingFile;
    private final String stagingFileCopyLocation;
    private final String validationErrorFile;
    private final String validationErrorFileUrl;
    private final StagingSchemaView schema;
    private final DatasetState status;
    private final Date lastUpdateTime;
    private final long jobExecutionId;
    private final String metadata;
    private final Long datasetId;
    
    /**
     * @return optional String representing the staging file copy location
     * Will return empty if the location is null which is the case for S3
     */
    @JsonIgnore
    public Optional<String> getStagingFileCopyLocation() {
        return Optional.ofNullable(stagingFileCopyLocation);
    }
}
