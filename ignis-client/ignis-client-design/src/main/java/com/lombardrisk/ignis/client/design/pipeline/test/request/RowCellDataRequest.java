package com.lombardrisk.ignis.client.design.pipeline.test.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class RowCellDataRequest {
    private final String inputDataValue;
}
