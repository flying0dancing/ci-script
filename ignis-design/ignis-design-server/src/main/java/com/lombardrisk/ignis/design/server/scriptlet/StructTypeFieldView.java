package com.lombardrisk.ignis.design.server.scriptlet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StructTypeFieldView {

    private String field;
    private String type;
}
