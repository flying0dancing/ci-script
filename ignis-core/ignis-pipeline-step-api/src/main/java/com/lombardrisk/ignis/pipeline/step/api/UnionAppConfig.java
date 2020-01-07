package com.lombardrisk.ignis.pipeline.step.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnionAppConfig {

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<SelectColumn> selects;

    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<String> filters;

    private UnionSpec schemaIn;
}
