package com.lombardrisk.ignis.functional.test.dsl;

import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.steps.JobSteps;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DeleteProductJobContext {

    private final ProductConfigView productConfigView;
    private final JobSteps jobSteps;

    public void waitForJobToSucceed() {
        jobSteps.waitForRollbackProductJobToPass(productConfigView.getName(), productConfigView.getVersion());
    }

    public void waitForJobToFail() {
        jobSteps.waitForRollbackProductJobToFail(productConfigView.getName(), productConfigView.getVersion());
    }
}
