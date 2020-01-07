import { EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { CellValueChangedEvent, ColDef, GridOptions, RowDataChangedEvent, RowEvent } from 'ag-grid';
import { ValueFormatterParams, ValueGetterParams } from 'ag-grid/dist/lib/entities/colDef';
import { DeleteButtonRendererComponent } from '../../core/grid/components/delete-button-renderer.component';
import { createDefaultGridOptions, createDefaultTextFilterOptions, updateGridOverlays } from '../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../core/grid/interfaces/delete-params.interface';
import { Field, Schema } from '../../schemas';
import { GridCellEditEvent } from '../interfaces/grid-cell-edit-event.interface';
import { GridRowDeleteEvent } from '../interfaces/grid-row-delete-event.interface';
import { InputData, OutputData, PipelineStepInputRows, PipelineStepTest } from '../interfaces/pipeline-step-test.interface';

type RowData = InputData | OutputData;

export abstract class AbstractPipelineStepTestDataGridComponent
  implements OnChanges {
  @Input() schema: Schema;

  @Input() pipelineStepTest: PipelineStepTest;

  @Input() inputData: PipelineStepInputRows;

  @Input() outputData: OutputData[];

  @Input() isLoading: boolean;

  @Output() cellEdit = new EventEmitter<GridCellEditEvent>();

  @Output() rowDelete = new EventEmitter<GridRowDeleteEvent>();

  rowData: RowData[];

  columnDefs: ColDef[];

  protected readonly defaultGridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    deltaRowDataMode: true,
    pagination: false,
    gridAutoHeight: false,
    getRowNodeId: data => data.id,
    onCellValueChanged: (event: CellValueChangedEvent) =>
      this.onCellValueChanged(event),
    onGridReady: (event: RowDataChangedEvent) => this.onGridReady(event),
    getRowStyle: (event: RowEvent) => this.getRowStyle(event)
  };

  gridOptions: GridOptions;

  constructor() {
    this.gridOptions = this.createGridOptions();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.isLoading || changes.inputData || changes.outputData || changes.schema) {
      updateGridOverlays(this.gridOptions.api, this.rowData, this.isLoading);
    }

    if (changes.schema) {
      this.columnDefs = this.formatColumnDefs();
    }

    if (changes.inputData || changes.outputData || changes.schema) {
      this.rowData = this.createRowData();

      if (this.gridOptions && this.gridOptions.columnApi) {
        setTimeout(() => this.gridOptions.columnApi.autoSizeAllColumns());
      }
    }
  }

  protected abstract createGridOptions(): GridOptions;

  protected abstract formatColumnDefs(): ColDef[];

  protected abstract createRowData(): RowData[];

  protected abstract getRowStyle(event: RowEvent): { [key: string]: string };

  protected createDeleteColumnDefinition() {
    return {
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this row?',
        gridDeleteCallback: params =>
          this.rowDelete.emit({
            pipelineStepId: this.pipelineStepTest.id,
            rowId: params.data.id
          })
      } as DeleteRendererParams,
      suppressResize: true,
      suppressAutoSize: true,
      suppressMenu: true,
      width: 100,
      minWidth: 100,
      equals: () => false
    };
  }

  protected createFieldColumnDefinition(field: Field): ColDef {
    return {
      ...createDefaultTextFilterOptions(),
      field: `${field.id}`,
      headerName: field.name,
      cellEditorParams: {
        useFormatter: true
      },
      editable: true,
      equals: () => false,
      valueGetter: (params: ValueGetterParams) =>
        this.getCellValue(params.data, field.id),
      valueFormatter: (params: ValueFormatterParams) =>
        this.getCellDisplayValue(params.data, field.id),
      filterValueGetter: (params: ValueGetterParams) =>
        this.getCellDisplayValue(params.data, field.id)
    };
  }

  private onGridReady(event: RowDataChangedEvent) {
    event.columnApi.autoSizeAllColumns();

    updateGridOverlays(this.gridOptions.api, this.rowData, this.isLoading);
  }

  private onCellValueChanged(event: CellValueChangedEvent) {
    const pipelineStepId = this.pipelineStepTest.id;
    const rowId = event.data.id;
    const fieldId = parseInt(event.colDef.field, 10);
    const cellId = event.value;
    const cellValue = event.data[fieldId];
    const previousCellValue = event.data.cells[fieldId].data;
    const cell = {
      id: cellId,
      data: cellValue
    };

    if (cellValue !== previousCellValue) {
      this.emitCellEditEvent(pipelineStepId, rowId, fieldId, cell);
    }
  }

  protected abstract emitCellEditEvent(pipelineStepId, rowId, fieldId, cell);

  private getCellValue(data: RowData, fieldId: number): number {
    return data && data.cells[fieldId].id;
  }

  private getCellDisplayValue(data: RowData, fieldId: number): string {
    const value = data ? data.cells[fieldId].data : null;

    return value !== null ? value : '';
  }
}
