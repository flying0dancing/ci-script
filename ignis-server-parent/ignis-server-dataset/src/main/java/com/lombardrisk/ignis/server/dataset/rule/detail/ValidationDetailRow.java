package com.lombardrisk.ignis.server.dataset.rule.detail;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

@Builder
@Data
public class ValidationDetailRow {

    @Singular
    private final Map<String, Object> values;
}
