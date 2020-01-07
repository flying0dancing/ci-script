import { Component, Input } from '@angular/core';
import { ApiError } from '../../../../core/utilities/interfaces/errors.interface';

@Component({
  selector: 'dv-step-form-errors',
  styleUrls: ['./pipeline-step-form-errors.component.scss'],
  templateUrl: './pipeline-step-form-errors.component.html'
})
export class PipelineStepFormErrorsComponent {
  @Input()
  apiErrors: ApiError[] = [];

}
