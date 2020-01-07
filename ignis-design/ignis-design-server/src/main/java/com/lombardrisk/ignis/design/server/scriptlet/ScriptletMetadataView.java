package com.lombardrisk.ignis.design.server.scriptlet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScriptletMetadataView {

    private Map<String, List<StructTypeFieldView>> inputs;
    private List<StructTypeFieldView> outputs;
}
