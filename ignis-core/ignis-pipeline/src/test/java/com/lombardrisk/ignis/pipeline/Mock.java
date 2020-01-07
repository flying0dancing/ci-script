package com.lombardrisk.ignis.pipeline;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Set;

public class Mock {

    @Data
    @Builder
    public static class Pipeline implements TransformationPipeline<PipelineStep> {
        private final Set<PipelineStep> steps;

        @Override
        public Set<PipelineStep> getSteps() {
            return steps;
        }
    }

    @Data
    @Builder
    public static class PipelineStep implements TransformationStep<Dataset> {
        private final String stepName;
        @Singular
        private final Set<Dataset> datasetIns;
        private final Dataset datasetOut;

        @Override
        public String getName() {
            return stepName;
        }

        @Override
        public Set<Dataset> getInputs() {
            return datasetIns;
        }

        @Override
        public Dataset getOutput() {
            return datasetOut;
        }
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Dataset {
        private final String name;

        public static Dataset withName(final String datasetName) {
            return new Dataset(datasetName);
        }
    }
}
