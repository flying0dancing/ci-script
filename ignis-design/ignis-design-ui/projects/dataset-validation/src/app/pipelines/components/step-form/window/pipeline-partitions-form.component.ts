import {
  Component,
  Input,
  OnChanges,
  OnDestroy,
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

interface PipelinePartitionsForm {
  partitionValue: string;
  partitionsArray: string[];
}

@Component({
  selector: 'dv-partitions-form',
  templateUrl: './pipeline-partitions-form.component.html',
  providers: subformComponentProviders(PipelinePartitionsFormComponent)
})
export class PipelinePartitionsFormComponent
  extends NgxSubFormRemapComponent<string[], PipelinePartitionsForm>
  implements OnInit, OnChanges, OnDestroy {
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

  ngOnDestroy(): void {
    //no-op
  }

  protected transformToFormGroup(obj: string[] | null): PipelinePartitionsForm {
    return {
      partitionsArray: obj,
      partitionValue: null
    };
  }

  protected transformFromFormGroup(
    formValue: PipelinePartitionsForm
  ): string[] | null {
    return formValue.partitionsArray;
  }

  protected getFormControls(): Controls<PipelinePartitionsForm> {
    return {
      partitionValue: new FormControl(null),
      partitionsArray: new FormArray([])
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
