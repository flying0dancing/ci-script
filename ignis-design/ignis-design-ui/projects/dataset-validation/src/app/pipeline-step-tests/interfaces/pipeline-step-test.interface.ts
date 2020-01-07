export enum PipelineStepTestStatus {
  Pass = 'PASS',
  Fail = 'FAIL',
  Pending = 'PENDING'
}

export enum PipelineStepTestRunStatus {
  Pass = 'PASS',
  Fail = 'FAIL'
}

export enum OutputDataType {
  Actual = 'ACTUAL',
  Expected = 'EXPECTED'
}

export enum OutputDataStatus {
  Matched = 'MATCHED',
  NotFound = 'NOT_FOUND',
  Unexpected = 'UNEXPECTED'
}

export interface DataCell {
  id: number;
  data: string;
}

export interface InputData {
  id: number;
  cells: {
    // The map is keyed by every fieldId
    [key: number]: DataCell;
  };
  run: boolean; // Has the data point been run through the system
}

export interface OutputData extends InputData {
  type: OutputDataType;
  status: OutputDataStatus;
}

export interface PipelineStepTest {
  id: number;
  pipelineId: number;
  pipelineStepId: number;
  name: string;
  description: string;
  testReferenceDate: string;
  status: PipelineStepTestStatus;
}

export interface PipelineStepInputRows {
  // The map is keyed by every schemaId as join step can have multiple input schemas
  [key: number]: InputData[];
}

export interface Run {
  id: number;
  status: PipelineStepTestRunStatus;
}
