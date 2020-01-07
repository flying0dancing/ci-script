import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Schema } from '../../../../schemas';
import { Select, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';

export interface UnionForm {
  selects: Select[];
  filter: string;
  schemaIn: Schema;
}

export const emptyUnionForm: UnionForm = {
  selects: [],
  filter: '',
  schemaIn: null,
};


@Component({
  selector: 'dv-union-form',
  templateUrl: './pipeline-union-form.component.html',
  providers: subformComponentProviders(PipelineUnionFormComponent),
})
export class PipelineUnionFormComponent extends NgxSubFormRemapComponent<UnionForm, UnionForm> {

  @Input()
  schemaOut: Schema;

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  protected transformToFormGroup(obj: UnionForm): UnionForm {
    return obj;
  }

  protected transformFromFormGroup(formValue: UnionForm): UnionForm {
    return formValue;
  }

  protected getFormControls(): Controls<UnionForm> {
    return {
      selects: new FormControl(),
      filter: new FormControl(),
      schemaIn: new FormControl()
    };
  }

  resetSchemaErrors(): void {
  }

  schemaOutChange() {
    this.resetSchemaErrors();
  }
}
