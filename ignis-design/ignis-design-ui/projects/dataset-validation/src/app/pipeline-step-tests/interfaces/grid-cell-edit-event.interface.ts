import { DataCell } from './pipeline-step-test.interface';

export interface GridCellEditEvent {
  pipelineStepId: number;
  rowId: number;
  fieldId: number;
  cell: DataCell;
  inputRow: boolean;
}
