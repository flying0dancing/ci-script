import { Component, Input } from '@angular/core';
import { FormControl } from '@angular/forms';
import {
  Controls,
  NgxSubFormRemapComponent,
  subformComponentProviders
} from 'ngx-sub-form';
import { Schema, schemaVersionDisplay } from '../../../../schemas';
import {
  JoinField,
  JoinType
} from '../../../interfaces/pipeline-step.interface';

interface PipelineJoinForm {
  id: number;
  leftSchema: Schema;
  rightSchema: Schema;
  joinFields: JoinField[];
  joinType: JoinType;
}

@Component({
  selector: 'dv-single-join-form',
  templateUrl: './pipeline-single-join-form.component.html',
  styleUrls: ['./pipeline-single-join-form.component.scss'],
  providers: subformComponentProviders(PipelineSingleJoinFormComponent)
})
export class PipelineSingleJoinFormComponent extends NgxSubFormRemapComponent<PipelineJoinForm,
  PipelineJoinForm> {
  @Input()
  schemas: Schema[] = [];

  @Input()
  joinTypes: JoinType[];

  @Input()
  displayIndex: number;
  schemaDisplayNameFunction = schemaVersionDisplay;

  protected transformToFormGroup(obj: PipelineJoinForm): PipelineJoinForm {
    return obj;
  }

  protected transformFromFormGroup(
    formValue: PipelineJoinForm
  ): PipelineJoinForm {
    return formValue;
  }

  protected getFormControls(): Controls<PipelineJoinForm> {
    return {
      id: new FormControl(null),
      leftSchema: new FormControl(null),
      rightSchema: new FormControl(null),
      joinFields: new FormControl(null),
      joinType: new FormControl(null)
    };
  }

  schemaLeftValueChanged($event: Schema) {
    this.formGroupControls.leftSchema.setValue($event);
  }

  schemaRightValueChanged($event: Schema) {
    this.formGroupControls.rightSchema.setValue($event);
  }
}
