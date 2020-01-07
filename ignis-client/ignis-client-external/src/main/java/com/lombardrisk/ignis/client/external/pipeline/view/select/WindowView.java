package com.lombardrisk.ignis.client.external.pipeline.view.select;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class WindowView {

    private final Set<String> partitionBy;
    private final List<OrderView> orderBy;
}
