import { Component, Input, OnChanges, QueryList, SimpleChanges, ViewChildren } from '@angular/core';
import { FormArray, FormControl, Validators } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Schema, schemaVersionDisplay } from '../../../../schemas';
import {
  addMissingOrderToSelects,
  PipelineUnionMapStep,
  SelectResult,
  SelectsExecutionErrors,
  Union
} from '../../../interfaces/pipeline-step.interface';
import { emptySelect } from '../select/pipeline-single-select-form.component';
import { emptyUnionForm, PipelineUnionFormComponent, UnionForm } from './pipeline-union-form.component';

export interface UnionMapFormInterface {
  schemaIns: Schema[],
  schemaOut: Schema
  unions: UnionForm[]
}

export const emptyUnionsMapForm: UnionMapFormInterface = {
  schemaIns: [],
  schemaOut: null,
  unions: []
};


@Component({
  selector: 'dv-unions-step-sub-form',
  styleUrls: ['./pipeline-unions-step-sub-form.component.scss'],
  templateUrl: './pipeline-unions-step-sub-form.component.html',
  providers: subformComponentProviders(PipelineUnionsStepSubFormComponent),
})
export class PipelineUnionsStepSubFormComponent
  extends NgxSubFormRemapComponent<UnionMapFormInterface, UnionMapFormInterface>
  implements OnChanges {
  @ViewChildren(PipelineUnionFormComponent) viewChildren !: QueryList<PipelineUnionFormComponent>;

  @Input()
  schemas: Schema[];

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  selectErrorsPerSchema: { [key: number]: SelectResult[] } = {};
  schemaDisplayNameFunction: (schema: Schema) => string = schemaVersionDisplay;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.selectExecutionErrors) {
      this.handleSelectExecutionChanges((changes));
    }
  }

  private handleSelectExecutionChanges(changes: SimpleChanges) {
    const selectErrors: SelectsExecutionErrors = changes.selectExecutionErrors.currentValue;
    this.selectErrorsPerSchema = {};
    if (!!selectErrors && selectErrors.individualErrors !== null) {
      selectErrors.individualErrors.forEach((selectResult: SelectResult) => {
        if (!this.selectErrorsPerSchema[selectResult.unionSchemaId]) {
          this.selectErrorsPerSchema[selectResult.unionSchemaId] = [];
        }

        this.selectErrorsPerSchema[selectResult.unionSchemaId].push(selectResult);
      });
    }
  }

  protected transformToFormGroup(obj: UnionMapFormInterface): UnionMapFormInterface {
    return obj;
  }

  protected transformFromFormGroup(formValue: UnionMapFormInterface): UnionMapFormInterface {
    return formValue;
  }

  protected getFormControls(): Controls<UnionMapFormInterface> {
    return {
      schemaIns: new FormControl(null, Validators.required),
      schemaOut: new FormControl(null, Validators.required),
      unions: new FormArray([]),
    };
  }

  resetSchemaErrors(): void {
  }

  schemaOutChange() {
    this.resetSchemaErrors();
  }


  getSchemaInTabHeader(schema: Schema) {
    return schemaVersionDisplay(schema);
  }

  addUnionItem(schema: Schema) {

    const fields = this.formGroupControls.schemaOut.value ? this.formGroupControls.schemaOut.value.fields : [];
    this.formGroupControls.unions.push(new FormControl({
      ...emptyUnionForm,
      selects: fields.map((field, index) => {
        return { ...emptySelect, outputFieldId: field.id, order: index };
      }),
      schemaIn: schema
    }));
  }

  removeUnionItem($event: Schema) {
    let indexToRemove = -1;
    this.formGroupControls.unions.controls.forEach((value, index) => {
      const schema = value.value.schemaIn;
      if ($event.id === schema.id) {
        indexToRemove = index;
      }
    });

    this.formGroupControls.unions.removeAt(indexToRemove);
  }


  public static convertUnionComponent(obj: PipelineUnionMapStep, schemaIdMap): UnionMapFormInterface {
    return {
      schemaIns: Object.keys(obj.unions).map(schemaId => schemaIdMap[parseInt(schemaId, 10)]),
      schemaOut: schemaIdMap[obj.schemaOutId],
      unions: PipelineUnionsStepSubFormComponent.convertUnionFormComponent(obj.unions, schemaIdMap)
    };
  }

  public static convertUnionFormComponent(unions: { [key: number]: Union }, schemaIdMap: { [key: string]: Schema }): UnionForm[] {
    return Object.keys(unions).map(schemaIdStr => {

      const schemaId = parseInt(schemaIdStr, 10);
      const union: Union = unions[schemaId];

      const unionForm: UnionForm = {
        schemaIn: schemaIdMap[schemaId],
        selects: addMissingOrderToSelects(union.selects).sort((a, b) => a.order - b.order),
        filter: (union.filters && union.filters.length > 0) ? union.filters[0] : ''
      };
      return unionForm;
    });
  }

  getSelectErrorsForUnion(schemaIn: Schema): SelectsExecutionErrors {
    const id = !!schemaIn ? schemaIn.id : null;
    return { individualErrors: this.selectErrorsPerSchema[id], transformationParseErrors: [] };
  }

  unionHasErrors(schemaIn: Schema): boolean {
    const errors: SelectsExecutionErrors = this.getSelectErrorsForUnion(schemaIn);
    return (!!errors && !!errors.individualErrors);
  }

  schemaOutValueChanged($event: Schema) {
    this.formGroupControls.schemaOut.setValue($event);
  }
}
