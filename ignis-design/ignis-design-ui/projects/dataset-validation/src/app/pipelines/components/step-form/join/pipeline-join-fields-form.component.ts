import { Component, Input, OnInit } from '@angular/core';
import { FormArray, FormControl, Validators } from '@angular/forms';
import {
  ArrayPropertyKey,
  ArrayPropertyValue,
  Controls,
  NgxFormWithArrayControls,
  NgxSubFormRemapComponent,
  subformComponentProviders
} from 'ngx-sub-form';
import { Schema } from '../../../../schemas';
import { JoinField } from '../../../interfaces/pipeline-step.interface';

interface PipelineJoinFieldsForm {
  joinFields: JoinField[];
}

@Component({
  selector: 'dv-join-fields-form',
  templateUrl: './pipeline-join-fields-form.component.html',
  styleUrls: ['./pipeline-join-fields-form.component.scss'],
  providers: subformComponentProviders(PipelineJoinFieldsFormComponent)
})
export class PipelineJoinFieldsFormComponent
  extends NgxSubFormRemapComponent<JoinField[], PipelineJoinFieldsForm>
  implements NgxFormWithArrayControls<PipelineJoinFieldsForm>, OnInit {
  @Input()
  leftSchema: Schema;

  @Input()
  rightSchema: Schema;

  ngOnInit(): void {
    this.formGroupControls.joinFields.push(
      this.createFormArrayControl('joinFields', {
        leftField: null,
        rightField: null
      })
    );
  }

  protected getFormControls(): Controls<PipelineJoinFieldsForm> {
    return {
      joinFields: new FormArray([])
    };
  }

  protected transformToFormGroup(
    obj: JoinField[] | null
  ): PipelineJoinFieldsForm {
    return {
      joinFields: obj ? obj : []
    };
  }

  protected transformFromFormGroup(
    formValue: PipelineJoinFieldsForm
  ): JoinField[] | null {
    return formValue.joinFields;
  }

  public removeJoinField(index: number): void {
    this.formGroupControls.joinFields.removeAt(index);
  }

  public addJoinField(): void {
    this.formGroupControls.joinFields.push(
      this.createFormArrayControl('joinFields', {
        leftField: null,
        rightField: null
      })
    );
  }

  public createFormArrayControl(
    key: ArrayPropertyKey<PipelineJoinFieldsForm> | undefined,
    value: ArrayPropertyValue<PipelineJoinFieldsForm>
  ): FormControl {
    switch (key) {
      // note: the following string is type safe based on your form properties!
      case 'joinFields':
        return new FormControl(value, [Validators.required]);
      default:
        return new FormControl(value);
    }
  }
}
