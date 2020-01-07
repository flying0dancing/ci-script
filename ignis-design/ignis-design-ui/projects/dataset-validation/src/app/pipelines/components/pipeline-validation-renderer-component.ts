import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';

@Component({
  templateUrl: './pipeline-validation-renderer-component.html',
  styleUrls: ['./pipeline-validation-renderer-component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineValidationRendererComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;
  valid: boolean;
  status: string;

  agInit(params: ICellRendererParams): void {
    this.params = params;
    const pipelineError = params.data.error;
    const isValid = pipelineError === null || pipelineError.errors.length === 0;
    this.valid = isValid;
    this.status = isValid ? 'VALID' : 'INVALID';
  }

  refresh(): boolean {
    return false;
  }
}
