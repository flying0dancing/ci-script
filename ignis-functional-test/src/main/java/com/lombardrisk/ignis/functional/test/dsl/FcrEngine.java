package com.lombardrisk.ignis.functional.test.dsl;

import com.lombardrisk.ignis.functional.test.assertions.HdfsAssertions;
import com.lombardrisk.ignis.functional.test.assertions.PhysicalSchemaAssertions;
import com.lombardrisk.ignis.functional.test.datasource.CsvFinder;
import com.lombardrisk.ignis.functional.test.steps.CleanupSteps;
import com.lombardrisk.ignis.functional.test.steps.JobSteps;
import com.lombardrisk.ignis.functional.test.steps.PipelineSteps;
import com.lombardrisk.ignis.functional.test.steps.ProductConfigSteps;
import com.lombardrisk.ignis.functional.test.steps.StagingSteps;
import com.lombardrisk.ignis.functional.test.steps.ValidationSteps;


public class FcrEngine {
    private final JobSteps jobSteps;
    private final ProductConfigSteps productConfigSteps;
    private final StagingSteps stagingSteps;
    private final PipelineSteps pipelineSteps;
    private final ValidationSteps validationSteps;
    private final CleanupSteps cleanupSteps;
    private final CsvFinder csvFinder;

    private final HdfsAssertions hdfsAssertions;
    private final PhysicalSchemaAssertions physicalSchemaAssertions;

    public FcrEngine(
            final JobSteps jobSteps,
            final ProductConfigSteps productConfigSteps,
            final StagingSteps stagingSteps,
            final PipelineSteps pipelineSteps,
            final ValidationSteps validationSteps,
            final CleanupSteps cleanupSteps,
            final CsvFinder csvFinder,
            final HdfsAssertions hdfsAssertions,
            final PhysicalSchemaAssertions physicalSchemaAssertions) {
        this.jobSteps = jobSteps;
        this.productConfigSteps = productConfigSteps;
        this.stagingSteps = stagingSteps;
        this.pipelineSteps = pipelineSteps;
        this.validationSteps = validationSteps;
        this.cleanupSteps = cleanupSteps;
        this.csvFinder = csvFinder;
        this.hdfsAssertions = hdfsAssertions;
        this.physicalSchemaAssertions = physicalSchemaAssertions;
    }

    public FcrEngineTest newTest() {
        return new FcrEngineTest(
                jobSteps,
                productConfigSteps,
                stagingSteps,
                pipelineSteps,
                validationSteps,
                cleanupSteps,
                csvFinder,
                hdfsAssertions,
                physicalSchemaAssertions);
    }
}
