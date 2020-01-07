import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import { FormArray, FormControl, ValidatorFn } from '@angular/forms';
import {
  Controls,
  NgxSubFormRemapComponent,
  subformComponentProviders
} from 'ngx-sub-form';
import { CommonValidators } from '../../../../core/forms/common.validators';
import { Schema } from '../../../../schemas';

interface PipelineGroupingsForm {
  groupingValue: string;
  groupingsArray: string[];
}

@Component({
  selector: 'dv-groupings-form',
  templateUrl: './pipeline-groupings-form.component.html',
  providers: subformComponentProviders(PipelineGroupingsFormComponent) // <-- Add this
})
export class PipelineGroupingsFormComponent
  extends NgxSubFormRemapComponent<string[], PipelineGroupingsForm>
  implements OnInit, OnChanges {
  @Input()
  inputSchema: Schema;

  schemaInFieldNames: string[];
  schemaInFieldNameValidator: ValidatorFn;

  ngOnInit(): void {
    this.setSchemaInProperties(this.inputSchema);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.inputSchema) {
      this.setSchemaInProperties(changes.inputSchema.currentValue);
    }
  }

  protected transformToFormGroup(obj: string[] | null): PipelineGroupingsForm {
    return {
      groupingsArray: obj,
      groupingValue: null
    };
  }

  protected transformFromFormGroup(
    formValue: PipelineGroupingsForm
  ): string[] | null {
    return formValue.groupingsArray;
  }

  protected getFormControls(): Controls<PipelineGroupingsForm> {
    return {
      groupingValue: new FormControl(null),
      groupingsArray: new FormArray([])
    };
  }

  private setSchemaInProperties(schema: Schema): void {
    if (!!schema) {
      this.inputSchema = schema;
      this.schemaInFieldNames = schema.fields.map(field => field.name);
      this.schemaInFieldNameValidator = CommonValidators.requireMatch(
        this.schemaInFieldNames
      );
    }
  }
}
