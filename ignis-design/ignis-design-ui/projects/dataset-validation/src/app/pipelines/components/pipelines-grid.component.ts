import { Component, Input } from '@angular/core';
import { ColDef, GridOptions, GridReadyEvent } from 'ag-grid';
import { DeleteButtonRendererComponent } from '../../core/grid/components/delete-button-renderer.component';
import {
  createDefaultGridOptions,
  createDefaultTextFilterOptions
} from '../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../core/grid/interfaces/delete-params.interface';
import * as pipelines from '../../pipelines/actions/pipelines.actions';
import { Pipeline } from '../interfaces/pipeline.interface';
import { EditPipelineButtonRendererComponent } from './edit-pipeline-button-renderer.component';
import { PipelineValidationRendererComponent } from './pipeline-validation-renderer-component';

@Component({
  selector: 'dv-pipelines-grid',
  templateUrl: './pipelines-grid.component.html'
})
export class PipelinesGridComponent {
  @Input() productId: number;
  @Input() pipelines: Pipeline[] = [];
  @Input() loading: boolean;

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    suppressCellSelection: true,
    paginationPageSize: 12,
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent)
  };

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Name',
      field: 'name',
      cellRendererFramework: EditPipelineButtonRendererComponent
    },
    {
      headerName: 'Valid',
      cellRendererFramework: PipelineValidationRendererComponent
    },
    {
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this pipeline?',
        fieldNameForMessage: 'name',
        gridDeleteAction: new pipelines.Delete(0)
      } as DeleteRendererParams,
      suppressResize: true,
      suppressMenu: true,
      width: 40
    }
  ];

  private onGridReady(readyEvent: GridReadyEvent) {
    readyEvent.api.sizeColumnsToFit();

    if (this.loading) {
      readyEvent.api.showLoadingOverlay();
    } else {
      if (this.pipelines.length === 0) {
        readyEvent.api.showNoRowsOverlay();
      }
    }
  }
}
