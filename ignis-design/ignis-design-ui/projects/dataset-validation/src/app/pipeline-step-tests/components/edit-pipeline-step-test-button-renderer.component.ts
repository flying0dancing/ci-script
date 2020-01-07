import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { PipelineStepTestGridCellRendererParams } from './pipeline-step-tests-grid.component';
import { PipelineStepTest } from '../interfaces/pipeline-step-test.interface';
import { PipelineStepTestEventService } from '../services/pipeline-step-test-event.service';

@Component({
  templateUrl: './edit-pipeline-step-test-button-renderer.component.html',
  styleUrls: ['./edit-pipeline-step-test-button-renderer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditPipelineStepTestButtonRendererComponent
  implements ICellRendererAngularComp {
  params: PipelineStepTestGridCellRendererParams;

  cellData: PipelineStepTest;

  cellValue: string;

  hover: boolean;

  constructor(
    private pipelineStepTestEventService: PipelineStepTestEventService
  ) {}

  agInit(params: PipelineStepTestGridCellRendererParams): void {
    this.params = params;
    this.cellData = params.data;
    this.cellValue = params.value;
  }

  refresh(): boolean {
    return false;
  }

  onClick(params: PipelineStepTestGridCellRendererParams) {
    this.pipelineStepTestEventService.editEvent.next({
      pipelineStepTest: params.data
    });
  }
}
