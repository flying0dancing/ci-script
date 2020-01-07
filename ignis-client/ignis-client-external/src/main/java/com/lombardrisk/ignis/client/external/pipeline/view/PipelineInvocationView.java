package com.lombardrisk.ignis.client.external.pipeline.view;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lombardrisk.ignis.common.json.LocalDateDeserializer;
import com.lombardrisk.ignis.common.json.LocalDateSerializer;
import com.lombardrisk.ignis.common.json.ZonedLocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PipelineInvocationView {

    private Long id;
    private String name;
    private Long pipelineId;
    private String entityCode;

    @JsonSerialize(using = ZonedLocalDateTimeSerializer.class)
    private ZonedDateTime createdTime;

    private Long serviceRequestId;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate referenceDate;
    private List<PipelineStepInvocationView> invocationSteps;
}
