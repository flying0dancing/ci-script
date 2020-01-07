package com.lombardrisk.ignis.client.design.pipeline.test.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class UpdateStepTestRequest {
    private final String name;
    private final String description;
    private final LocalDate testReferenceDate;
}
