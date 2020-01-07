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
import {
  Order,
  OrderDirection
} from '../../../interfaces/pipeline-step.interface';

interface PipelineOrdersForm {
  orderByValue: string;
  orderByArray: Order[];
}

@Component({
  selector: 'dv-orders-form',
  templateUrl: './pipeline-order-by-form.component.html',
  providers: subformComponentProviders(PipelineOrderByFormComponent) // <-- Add this
})
export class PipelineOrderByFormComponent
  extends NgxSubFormRemapComponent<Order[], PipelineOrdersForm>
  implements OnInit, OnChanges, OnDestroy {
  private orderDirections: string[] = [OrderDirection.ASC, OrderDirection.DESC];

  @Input()
  inputSchema: Schema;

  schemaIn: Schema;
  schemaInFieldNames: string[];
  schemaInFieldNameValidator: ValidatorFn;
  orderByFields: string[];
  orderByFieldValidator: ValidatorFn;

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

  protected transformToFormGroup(obj: Order[] | null): PipelineOrdersForm {
    return {
      orderByArray: obj,
      orderByValue: null
    };
  }

  protected transformFromFormGroup(
    formValue: PipelineOrdersForm
  ): Order[] | null {
    return formValue.orderByArray;
  }

  protected getFormControls(): Controls<PipelineOrdersForm> {
    return {
      orderByValue: new FormControl(null),
      orderByArray: new FormArray([])
    };
  }

  private setSchemaInProperties(schema: Schema): void {
    if (schema) {
      this.inputSchema = schema;
      this.schemaInFieldNames = schema.fields.map(field => field.name);
      this.schemaInFieldNameValidator = CommonValidators.requireMatch(
        this.schemaInFieldNames
      );

      this.orderByFields = this.getFieldNamesWithDirection();
      this.orderByFieldValidator = CommonValidators.requireMatch(
        this.orderByFields
      );
    }
  }

  private getFieldNamesWithDirection(): string[] {
    const fieldNamesWithDirections: string[] = [];

    for (const fieldName of this.schemaInFieldNames) {
      for (const direction of this.orderDirections) {
        fieldNamesWithDirections.push(`${fieldName} ${direction}`);
      }
    }

    return fieldNamesWithDirections;
  }
}
