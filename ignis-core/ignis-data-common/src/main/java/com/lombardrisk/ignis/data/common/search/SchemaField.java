package com.lombardrisk.ignis.data.common.search;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SchemaField {
    private String name;
    private String type;
}