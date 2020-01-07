import { Pipeline } from '../../pipelines/interfaces/pipeline.interface';
import { ProductConfig } from '../../product-configs/interfaces/product-config.interface';
import { Schema } from '../../schemas';
import {
  OutputDataStatus,
  PipelineStepInputRows,
  PipelineStepTest,
  PipelineStepTestStatus
} from '../interfaces/pipeline-step-test.interface';

export interface EditPipelineStepTestUrl {
  productId: number;
  pipelineId: number;
  pipelineStepTestId: number;
}

export const formatEditPipelineStepTestUrl = ({
  productId,
  pipelineId,
  pipelineStepTestId
}: EditPipelineStepTestUrl): string =>
  `product-configs/${productId}/pipelines/${pipelineId}/stepTests/${pipelineStepTestId}`;

export const getPipelineStepTestStep = (
  pipeline: Pipeline,
  pipelineStepId: number
): string => {
  const matchedStep =
    pipeline && pipeline.steps.find(s => s.id === pipelineStepId);

  return matchedStep ? matchedStep.name : null;
};

export const getOverallPipelineStepTestsStatus = (
  pipelineStepTests: PipelineStepTest[]
): PipelineStepTestStatus => {
  let successCount = 0;
  let failCount = 0;
  let pendingCount = 0;

  pipelineStepTests.forEach(pipelineStep => {
    switch (pipelineStep.status) {
      case PipelineStepTestStatus.Pass:
        successCount += 1;
        break;
      case PipelineStepTestStatus.Fail:
        failCount += 1;
        break;
      case PipelineStepTestStatus.Pending:
        pendingCount += 1;
        break;
    }
  });

  if (failCount) {
    return PipelineStepTestStatus.Fail;
  } else if (pendingCount) {
    return PipelineStepTestStatus.Pending;
  } else if (successCount) {
    return PipelineStepTestStatus.Pass;
  } else {
    return null;
  }
};

export const getPipelineStepTestStepSchemas = (
  inputData: PipelineStepInputRows,
  product: ProductConfig
): Schema[] => {
  if (product && inputData) {
    const schemaIds = Object.keys(inputData).map(keys =>
      parseInt(keys, 10)
    );
    const productSchemas = product.schemas;

    return schemaIds.map(id => productSchemas.find(s => s.id === id));
  }

  return [];
};

export const getSchemaOutId = (
  pipelineStepTest: PipelineStepTest,
  pipeline: Pipeline
): number => {
  if (!pipeline || !pipelineStepTest) return null;

  const pipelineStep = pipeline.steps.find(
    step => step.id === pipelineStepTest.pipelineStepId
  );

  return pipelineStep !== null ? pipelineStep.schemaOutId : null;
};

export interface Appearance {
  displayText: string;
  backgroundColor: string;
  iconColor: string;
  iconName: string;
}

export interface OutputDataStatusAppearanceMap {
  [key: string]: Appearance;
}

export const OUTPUT_DATA_STATUS_APPEARANCE_MAP: OutputDataStatusAppearanceMap = {
  [OutputDataStatus.Matched]: {
    displayText: 'Matched',
    backgroundColor: '#ecffec',
    iconColor: 'green',
    iconName: 'done'
  },
  [OutputDataStatus.Unexpected]: {
    displayText: 'Unexpected',
    backgroundColor: '#f7e6e6',
    iconColor: 'red',
    iconName: 'error'
  },
  [OutputDataStatus.NotFound]: {
    displayText: 'Not Found',
    backgroundColor: '#fbfaf3',
    iconColor: 'gold',
    iconName: 'warning'
  }
};

export const DATA_NOT_RUN_APPEARANCE = {
  displayText: 'Not Run',
  backgroundColor: '#f1fbff',
  iconColor: 'initial',
  iconName: 'timer'
};
