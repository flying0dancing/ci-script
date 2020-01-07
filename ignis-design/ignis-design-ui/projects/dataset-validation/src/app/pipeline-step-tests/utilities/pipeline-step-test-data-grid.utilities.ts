import { EventEmitter } from '@angular/core';
import {
  ValueGetterParams,
  ValueFormatterParams
} from 'ag-grid/dist/lib/entities/colDef';
import { createDefaultTextFilterOptions } from '../../core/grid/grid.functions';
import { GridRowDeleteEvent } from '../interfaces/grid-row-delete-event.interface';
import {
  OutputData,
  InputData
} from '../interfaces/pipeline-step-test.interface';
import { DeleteButtonRendererComponent } from '../../core/grid/components/delete-button-renderer.component';
import { DeleteRendererParams } from '../../core/grid/interfaces/delete-params.interface';
import { Field } from '../../schemas';

export const createFieldColumnDefinition = (field: Field) => ({
  ...createDefaultTextFilterOptions(),
  field: `${field.id}`,
  headerName: field.name,
  cellEditorParams: {
    useFormatter: true
  },
  editable: true,
  equals: () => false,
  valueGetter: (params: ValueGetterParams) => getValue(params.data, field.id),
  valueFormatter: (params: ValueFormatterParams) =>
    getDisplayValue(params.data, field.id),
  filterValueGetter: (params: ValueGetterParams) =>
    getDisplayValue(params.data, field.id)
});

export const getValue = (
  data: InputData | OutputData,
  fieldId: number
): number => {
  return data.cells[fieldId].id;
};

export const getDisplayValue = (
  outputData: InputData | OutputData,
  fieldId: number
): string => {
  const value = outputData.cells[fieldId].data;

  return value !== null ? value : '';
};

export const createDeleteColumnDefinition = (
  rowDeleteEmitter: EventEmitter<GridRowDeleteEvent>,
  pipelineStepId: number
) => ({
  cellRendererFramework: DeleteButtonRendererComponent,
  cellRendererParams: {
    title: 'Are you sure you want to delete this row?',
    gridDeleteCallback: params =>
      rowDeleteEmitter.emit({
        pipelineStepId,
        rowId: params.data.id
      })
  } as DeleteRendererParams,
  suppressResize: true,
  suppressAutoSize: true,
  suppressMenu: true,
  width: 100,
  minWidth: 100,
  equals: () => false
});
