import {
  Component,
  Input,
  OnChanges,
  SimpleChanges,
  ChangeDetectionStrategy,
  Output,
  EventEmitter
} from '@angular/core';
import {
  GridOptions,
  ColDef,
  ICellRendererParams,
  RowDataChangedEvent,
  GridApi
} from 'ag-grid';
import { EditPipelineStepTestButtonRendererComponent } from './edit-pipeline-step-test-button-renderer.component';
import { PipelineStepTestStatusRendererComponent } from './pipeline-step-test-status-renderer.component';
import { PipelineStepTest } from '../interfaces/pipeline-step-test.interface';
import { getPipelineStepTestStep } from '../utilities/pipeline-step-tests.utilities';
import { Pipeline } from '../../pipelines/interfaces/pipeline.interface';
import { DeleteButtonRendererComponent } from '../../core/grid/components/delete-button-renderer.component';
import { DeleteRendererParams } from '../../core/grid/interfaces/delete-params.interface';
import {
  createDefaultTextFilterOptions,
  createDefaultSetFilterOptions,
  createDefaultGridOptions,
  updateGridOverlays
} from '../../core/grid/grid.functions';

export interface PipelineStepTestGridCellRendererParams
  extends ICellRendererParams {
  data: PipelineStepTest;
  context: {
    pipeline: Pipeline;
  };
}

export interface RowDeleteEvent {
  id: number;
}

@Component({
  selector: 'dv-pipeline-step-tests-grid',
  templateUrl: './pipeline-step-tests-grid.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestsGridComponent implements OnChanges {
  @Input() pipeline: Pipeline;

  @Input() pipelineStepTests: PipelineStepTest[];

  @Input() isLoading = false;

  @Output() rowDelete = new EventEmitter<RowDeleteEvent>();

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    deltaRowDataMode: true,
    suppressCellSelection: true,
    context: {
      pipeline: null
    },
    getRowNodeId: data => data.id,
    onRowDataChanged: (event: RowDataChangedEvent) =>
      this.onRowDataChanged(event)
  };

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Name',
      field: 'name',
      cellRendererFramework: EditPipelineStepTestButtonRendererComponent
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Step',
      field: 'pipelineStepId',
      valueFormatter: this.pipelineStepIdValueGetter
    },
    {
      ...createDefaultSetFilterOptions(),
      headerName: 'Status',
      field: 'status',
      cellRendererFramework: PipelineStepTestStatusRendererComponent
    },
    {
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this pipeline step test?',
        fieldNameForMessage: 'name',
        gridDeleteCallback: params =>
          this.rowDelete.emit({
            id: params.data.id
          })
      } as DeleteRendererParams,
      suppressResize: true,
      suppressMenu: true,
      width: 60,
      equals: () => false
    }
  ];

  ngOnChanges(changes: SimpleChanges) {
    if (changes.pipeline) {
      this.updateGridContext();
    }

    if (changes.isLoading || changes.pipelineStepTests) {
      updateGridOverlays(
        this.gridOptions.api,
        this.pipelineStepTests,
        this.isLoading
      );
    }
  }

  private onRowDataChanged(event: RowDataChangedEvent) {
    event.api.sizeColumnsToFit();

    updateGridOverlays(event.api, this.pipelineStepTests, this.isLoading);
    this.updateGridContext();
  }

  private pipelineStepIdValueGetter(
    params: PipelineStepTestGridCellRendererParams
  ) {
    const pipeline = params.context.pipeline;
    const pipelineStepId = params.value;

    return getPipelineStepTestStep(pipeline, pipelineStepId);
  }

  private updateGridContext() {
    if (!this.gridOptions.api) return;

    this.gridOptions.context = {
      pipeline: this.pipeline
    };

    this.gridOptions.api.refreshCells({ force: true });
  }
}
