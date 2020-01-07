import { Component, ChangeDetectionStrategy } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ICellRendererParams } from 'ag-grid';
import { OutputData } from '../interfaces/pipeline-step-test.interface';

@Component({
  templateUrl:
    './pipeline-step-test-output-data-row-status-renderer.component.html',
  styleUrls: [
    './pipeline-step-test-output-data-row-status-renderer.component.scss'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestOutputDataRowStatusRendererComponent
  implements ICellRendererAngularComp {
  outputData: OutputData;

  agInit(params: ICellRendererParams): void {
    this.outputData = params.data;
  }

  refresh(): boolean {
    return false;
  }
}
