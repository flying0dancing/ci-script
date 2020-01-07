import { Dataset, GetDatasetsResponse, ValidationStatus } from '../datasets.interfaces';
import { SourceFiles } from '../datasets.types';

export const dataset: Dataset = {
  id: 1,
  table: "table",
  tableId: 23,
  name: "test",
  datasetType: "staging",
  validationJobId: 5,
  pipelineJobId: 6,
  pipelineInvocationId: 7,
  pipelineStepInvocationId: 12,
  createdTime: "string",
  lastUpdated: "string",
  metadataKey: "string",
  metadata: "string",
  recordsCount: 9,
  rowKeySeed: 101,
  predicate: "string",
  entityCode: "string",
  referenceDate: "string",
  localReferenceDate: "1991-1-1",
  validationStatus: ValidationStatus.VALIDATED,
  hasRules: true,
  runKey: 1
};

export const datasets: Dataset[] = [dataset, dataset];

export const getResponseSuccess: GetDatasetsResponse = {
  _embedded: {
    datasetList: datasets
  }
};

export const sourceFiles: SourceFiles = ["a.csv", "b.csv"];
