import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { PipelineStepTestGridCellRendererParams } from './pipeline-step-tests-grid.component';
import { PipelineStepTestStatus } from '../interfaces/pipeline-step-test.interface';

@Component({
  templateUrl: './pipeline-step-test-status-renderer.component.html',
  styleUrls: ['./pipeline-step-test-status-renderer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestStatusRendererComponent
  implements ICellRendererAngularComp {
  cellValue: PipelineStepTestStatus;

  agInit(params: PipelineStepTestGridCellRendererParams): void {
    this.cellValue = params.value;
  }

  refresh(): boolean {
    return false;
  }
}
