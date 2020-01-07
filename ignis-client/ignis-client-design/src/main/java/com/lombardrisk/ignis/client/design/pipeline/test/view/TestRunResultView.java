package com.lombardrisk.ignis.client.design.pipeline.test.view;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TestRunResultView {

    private final Long id;
    private final StepTestStatus status;
}
