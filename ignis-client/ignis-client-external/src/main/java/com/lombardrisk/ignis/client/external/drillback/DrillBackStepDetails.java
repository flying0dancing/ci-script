package com.lombardrisk.ignis.client.external.drillback;

import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder
public class DrillBackStepDetails {

    private final Map<Long, List<FieldView>> schemasIn;
    private final Map<Long, List<FieldView>> schemasOut;
    private final Map<String, Map<String, String>> schemaToInputFieldToOutputFieldMapping;
}
