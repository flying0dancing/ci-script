import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import {
  Controls,
  NgxSubFormComponent,
  subformComponentProviders
} from 'ngx-sub-form';
import { Field } from '../../../../schemas';
import { JoinField } from '../../../interfaces/pipeline-step.interface';

@Component({
  selector: 'dv-join-field-form',
  templateUrl: './pipeline-join-field-form.component.html',
  providers: subformComponentProviders(PipelineJoinFieldFormComponent)
})
export class PipelineJoinFieldFormComponent extends NgxSubFormComponent<
  JoinField
> {
  @Input()
  leftSchemaFields: Field[];

  @Input()
  rightSchemaFields: Field[];
  fieldNameFunction: (field: Field) => string = field => field.name;

  protected getFormControls(): Controls<JoinField> {
    return {
      leftField: new FormControl(null),
      rightField: new FormControl(null)
    };
  }

  leftFieldValueChanged($event: Field) {
    this.formGroupControls.leftField.setValue($event);
  }
  rightFieldValueChanged($event: Field) {
    this.formGroupControls.rightField.setValue($event);
  }
}
