import { Component, Input } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Field, Schema, schemaVersionDisplay } from '../../../../schemas';
import { PipelineAggregationStep, Select, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';

export interface AggregationFormInterface {
  selects: Select[];
  filter: string;
  groupings: string[];
  schemaIn: Schema;
  schemaOut: Schema;
}

export const emptyAggregationForm: AggregationFormInterface = {
  selects: [],
  filter: '',
  schemaIn: null,
  schemaOut: null,
  groupings: []
};

@Component({
  selector: 'dv-aggregation-step-sub-form',
  styleUrls: ['../pipeline-step-root.component.scss'],
  templateUrl: './pipeline-aggregation-step-sub-form.component.html',
  providers: subformComponentProviders(PipelineAggregationStepSubFormComponent)
})
export class PipelineAggregationStepSubFormComponent extends NgxSubFormRemapComponent<
  AggregationFormInterface,
  AggregationFormInterface
> {
  @Input()
  schemas: Schema[];

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  outputFields: Field[] = [];
  schemaDisplayNameFunction = schemaVersionDisplay;

  protected transformToFormGroup(
    obj: AggregationFormInterface
  ): AggregationFormInterface {
    return obj;
  }

  protected transformFromFormGroup(
    formValue: AggregationFormInterface
  ): AggregationFormInterface {
    return formValue;
  }

  protected getFormControls(): Controls<AggregationFormInterface> {
    return {
      selects: new FormControl(null),
      filter: new FormControl(null),
      groupings: new FormControl(null),
      schemaIn: new FormControl(null, Validators.required),
      schemaOut: new FormControl(null, Validators.required)
    };
  }

  resetSchemaErrors(): void {}

  schemaOutChange(schemaOut) {
    this.resetSchemaErrors();

    if (!!schemaOut.value) {
      this.outputFields = schemaOut.value.fields;
    }
  }

  public static convertAggregationComponent(
    obj: PipelineAggregationStep,
    schemaIdMap
  ): AggregationFormInterface {
    return {
      selects: obj.selects,
      groupings: obj.groupings,
      filter: obj.filters && obj.filters.length > 0 ? obj.filters[0] : '',
      schemaIn: schemaIdMap[obj.schemaInId],
      schemaOut: schemaIdMap[obj.schemaOutId]
    };
  }

  schemaInValueChanged(schema: Schema) {
    this.formGroupControls.schemaIn.setValue(schema);
  }

  schemaOutValueChanged(schema: Schema) {
    this.formGroupControls.schemaOut.setValue(schema);
  }

  getInputSchemas() {
    return this.formGroupControls.schemaIn.value
      ? [this.formGroupControls.schemaIn.value]
      : [];
  }

  getInputSchemasMetadata() {
    return this.formGroupControls.schemaIn.value
      ? [{
        physicalTableName: this.formGroupControls.schemaIn.value.physicalTableName,
        joinSchemaType: '',
        aggregationSchemaType: 'input',
        aggregationGroupingFieldNames: this.formGroupControls.groupings.value,
        fieldNames: []
    }] : []
  }
}
