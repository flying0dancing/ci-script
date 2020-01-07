import { Component, Input } from '@angular/core';
import { ApiError } from '../../../../core/utilities/interfaces/errors.interface';

@Component({
  selector: 'dv-step-form-single-error',
  styleUrls: ['./pipeline-step-form-errors.component.scss'],
  templateUrl: './pipeline-step-form-single-error.component.html'
})
export class PipelineStepFormSingleErrorComponent {
  @Input()
  apiError: ApiError;

  newLineToArray(errorMessage: string): string[] {
    return errorMessage.split('\n');
  }
}
