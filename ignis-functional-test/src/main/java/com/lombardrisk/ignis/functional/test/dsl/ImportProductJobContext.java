package com.lombardrisk.ignis.functional.test.dsl;

import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.functional.test.steps.JobSteps;
import com.lombardrisk.ignis.functional.test.steps.ProductConfigSteps;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ImportProductJobContext {
    private final ProductConfigView productConfig;
    private final ProductConfigSteps productConfigSteps;
    private final JobSteps jobSteps;

    public ProductConfigView waitForJobToSucceed() {
        jobSteps.waitForImportProductJobToPass(productConfig.getName(), productConfig.getVersion());
        return productConfigSteps.findProduct(productConfig.getId());
    }

    public ProductConfigView waitForJobToFail() {
        jobSteps.waitForImportProductJobToFail(productConfig.getName(), productConfig.getVersion());
        return productConfigSteps.findProduct(productConfig.getId());
    }
}

