package com.lombardrisk.ignis.spark.pipeline.job;

import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.pipeline.service.PipelineStatusService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class PipelineJobOperator {

    private final PipelineStatusService pipelineStatusService;
    private final SparkConfigurationService sparkConfigurationService;
    private final PipelineStepExecutor pipelineStepExecutor;


    public PipelineJobOperator(
            final PipelineStatusService pipelineStatusService,
            final SparkConfigurationService sparkConfigurationService,
            final PipelineStepExecutor pipelineStepExecutor) {
        this.pipelineStatusService = pipelineStatusService;
        this.sparkConfigurationService = sparkConfigurationService;
        this.pipelineStepExecutor = pipelineStepExecutor;
    }

    public void runJob(final PipelineAppConfig pipelineAppConfig) {
        log.debug("Running pipeline job");
        Map<Long, DatasetTableLookup> outputDatasets = new HashMap<>();

        sparkConfigurationService.setupUserDefinedFunctions(pipelineAppConfig.getSparkFunctionConfig());

        for (PipelineStepAppConfig pipelineStepConfig : pipelineAppConfig.getPipelineSteps()) {
            final Long invocationId = pipelineAppConfig.getPipelineInvocationId();
            final Long stepInvocationId = pipelineStepConfig.getPipelineStepInvocationId();

            log.debug("Running invocation [{}], step invocation [{}]", invocationId, stepInvocationId);

            try {
                pipelineStatusService.setPipelineStepRunning(invocationId, stepInvocationId);

                DatasetTableLookup datasetTableLookup = pipelineStepExecutor.
                        runPipelineStep(pipelineAppConfig, pipelineStepConfig, outputDatasets);
                outputDatasets.put(stepInvocationId, datasetTableLookup);

                pipelineStatusService.setPipelineStepSuccess(invocationId, stepInvocationId);
            } catch (Exception e1) {
                log.error("Failed executing invocation [{}], step invocation [{}]",
                        invocationId, stepInvocationId, e1);
                try {
                    pipelineStatusService.setPipelineStepFailed(invocationId, stepInvocationId);
                } catch (IOException e2) {
                    log.error("Failed to set invocation [{}], step invocation [{}] as failed",
                            invocationId, stepInvocationId, e2);
                }
                throw new PipelineJobOperatorException(e1);
            }
        }
    }


}
