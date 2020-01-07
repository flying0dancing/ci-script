import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ColDef, GridOptions, RowEvent } from 'ag-grid';
import { OutputData, OutputDataType } from '../interfaces/pipeline-step-test.interface';
import { DATA_NOT_RUN_APPEARANCE, OUTPUT_DATA_STATUS_APPEARANCE_MAP } from '../utilities/pipeline-step-tests.utilities';
import { AbstractPipelineStepTestDataGridComponent } from './abstract-pipeline-step-test-data-grid.component';
import { PipelineStepTestOutputDataRowStatusRendererComponent } from './pipeline-step-test-output-data-row-status-renderer.component';

@Component({
  selector: 'dv-pipeline-step-test-output-data-grid',
  templateUrl: './abstract-pipeline-step-test-data-grid.component.html',
  styleUrls: ['./abstract-pipeline-step-test-data-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestOutputDataGridComponent extends AbstractPipelineStepTestDataGridComponent {
  protected createGridOptions(): GridOptions {
    return {
      ...this.defaultGridOptions,
      autoGroupColumnDef: {
        headerName: 'TYPE'
      },
      groupDefaultExpanded: 1,
      rememberGroupStateWhenNewData: true
    };
  }

  protected createRowData(): OutputData[] {
    return this.outputData ? this.outputData : [];
  }

  protected getRowStyle(event: RowEvent) {
    const hasRun = event.data ? event.data.run : false;

    if (event.node.group) return { background: 'initial' };

    if (!hasRun) return { background: DATA_NOT_RUN_APPEARANCE.backgroundColor };

    return {
      background:
        OUTPUT_DATA_STATUS_APPEARANCE_MAP[event.data.status].backgroundColor
    };
  }

  protected formatColumnDefs(): ColDef[] {
    if (!this.schema) return [];

    const fieldColDefs = this.schema.fields.map(field => ({
      ...this.createFieldColumnDefinition(field),
      editable: params => params.data.type === OutputDataType.Expected
    }));

    const deleteColDef = this.createDeleteColumnDefinition();
    deleteColDef.cellRendererParams.visible = params =>
      params.data && params.data.type === OutputDataType.Expected;

    return [
      {
        field: 'type',
        headerName: 'TYPE',
        hide: true,
        rowGroup: true,
        equals: () => false
      },
      {
        field: 'status',
        headerName: 'STATUS',
        equals: () => false,
        cellRendererFramework: PipelineStepTestOutputDataRowStatusRendererComponent
      },
      ...fieldColDefs,
      deleteColDef
    ];
  }

  protected emitCellEditEvent(pipelineStepId, rowId, fieldId, cell) {
    this.cellEdit.emit({pipelineStepId, rowId, fieldId, cell, inputRow: false });
  }
}
