import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ColDef, GridOptions, RowEvent } from 'ag-grid';
import { InputData } from '../interfaces/pipeline-step-test.interface';
import { DATA_NOT_RUN_APPEARANCE } from '../utilities/pipeline-step-tests.utilities';
import { AbstractPipelineStepTestDataGridComponent } from './abstract-pipeline-step-test-data-grid.component';

@Component({
  selector: 'dv-pipeline-step-test-input-data-grid',
  templateUrl: './abstract-pipeline-step-test-data-grid.component.html',
  styleUrls: ['./abstract-pipeline-step-test-data-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestInputDataGridComponent extends AbstractPipelineStepTestDataGridComponent {
  protected createGridOptions(): GridOptions {
    return this.defaultGridOptions;
  }

  protected createRowData(): InputData[] {
    return this.inputData && this.schema
      ? this.inputData[this.schema.id]
      : [];
  }

  protected getRowStyle(event: RowEvent) {
    const hasRun = event.data ? event.data.run : false;

    return {
      background: !hasRun ? DATA_NOT_RUN_APPEARANCE.backgroundColor : 'initial'
    };
  }

  protected formatColumnDefs(): ColDef[] {
    if (!this.schema) return [];

    const fieldColDefs = this.schema.fields.map(field =>
      this.createFieldColumnDefinition(field)
    );

    return [...fieldColDefs, this.createDeleteColumnDefinition()];
  }

  protected emitCellEditEvent(pipelineStepId, rowId, fieldId, cell) {
    this.cellEdit.emit({pipelineStepId, rowId, fieldId, cell, inputRow: true });
  }
}
