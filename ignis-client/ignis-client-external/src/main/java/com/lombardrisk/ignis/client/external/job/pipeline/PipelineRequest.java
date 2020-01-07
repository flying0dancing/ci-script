package com.lombardrisk.ignis.client.external.job.pipeline;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PipelineRequest {

    @NotBlank
    private String name;

    @NotBlank
    private final String entityCode;

    @NotNull
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private final LocalDate referenceDate;

    @NotNull
    private final Long pipelineId;

    private final Long stepId;
}
