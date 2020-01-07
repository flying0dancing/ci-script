import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import {
  Controls,
  NgxSubFormRemapComponent,
  subformComponentProviders
} from 'ngx-sub-form';
import { Schema } from '../../../../schemas';

interface PipelineFiltersForm {
  filter: string;
}

@Component({
  selector: 'dv-filters-form',
  styleUrls: ['./pipeline-filters-form.component.scss'],
  templateUrl: './pipeline-filters-form.component.html',
  providers: subformComponentProviders(PipelineFiltersFormComponent) // <-- Add this
})
export class PipelineFiltersFormComponent extends NgxSubFormRemapComponent<string, PipelineFiltersForm> {
  @Input()
  inputSchemas: Schema[];

  protected transformToFormGroup(obj: string | null): PipelineFiltersForm {
    return {
      filter: obj
    };
  }

  protected transformFromFormGroup(formValue: PipelineFiltersForm): string | null {
    return formValue.filter;
  }

  protected getFormControls(): Controls<PipelineFiltersForm> {
    return {
      filter: new FormControl(null)
    };
  }
}
