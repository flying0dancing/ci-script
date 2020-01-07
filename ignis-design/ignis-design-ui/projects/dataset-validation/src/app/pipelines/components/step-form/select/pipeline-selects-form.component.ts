import { Component, Input, OnChanges, OnDestroy, SimpleChanges, ViewChild } from '@angular/core';
import { FormArray, FormControl, ValidationErrors, Validators } from '@angular/forms';
import { MatCheckboxChange, MatPaginator, PageEvent } from '@angular/material';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { clearFormArray, flatten, moveElement } from '../../../../core/utilities';
import { Field, Schema, SchemaMetadata } from '../../../../schemas';
import { Select, SelectResult, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';
import { PipelineSelectsService } from './pipeline-selects.service';
import { emptySelect } from './pipeline-single-select-form.component';

interface PipelineSelectsForm {
  selects: Select[];
}

@Component({
  selector: 'dv-selects-form',
  templateUrl: './pipeline-selects-form.component.html',
  styleUrls: ['./pipeline-selects-form.component.scss'],
  providers: subformComponentProviders(PipelineSelectsFormComponent)
})
export class PipelineSelectsFormComponent extends NgxSubFormRemapComponent<Select[], PipelineSelectsForm> implements OnChanges, OnDestroy {
  @Input()
  inputSchemas: Schema[];

  @Input()
  inputSchemasMetadata: SchemaMetadata[];

  @Input()
  outputFields: Field[];

  @Input()
  allowWindow = false;

  @Input()
  unionSchemaIn: number | null;

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  @Input()
  showOrder = true;

  @ViewChild('paginator', { static: true }) paginatorComponent: MatPaginator;

  length = 0;
  pageSize = 3;
  currentPage = 0;
  pageSizeOptions: number[] = [1, 2, 3, 10];
  keyedSelectErrors: { [key: number]: SelectResult } = {};

  public pagingService: PipelineSelectsService;

  protected transformToFormGroup(obj: Select[] | null): PipelineSelectsForm {
    this.pagingService.reset();
    if (obj == null) {
      return { selects: obj };
    }

    return this.transformSelects(obj);
  }

  private transformSelects(obj: Select[] | null) {

    const fieldIdIndexedMap = new Map<number, Field>();
    this.outputFields.forEach(value => fieldIdIndexedMap.set(value.id, value));
    const selectsWithFields = obj.map(select => {
      return {
        ...select,
        outputField: fieldIdIndexedMap.get(select.outputFieldId)
      };
    });
    const missingSelects = this.populateMissingSelects(selectsWithFields);
    missingSelects.forEach(select => selectsWithFields.push(select));

    return {
      selects: selectsWithFields
    };
  }

  private populateMissingSelects(obj: Select[] | null) {
    const fieldIdToSelect = new Map<number, Select>();

    obj.forEach(select => fieldIdToSelect.set(select.outputFieldId, select));
    return this.outputFields.map((outputField, index) => {
      if (fieldIdToSelect.has(outputField.id)) {
        return null;
      }

      return {
        ...emptySelect,
        order: index,
        outputFieldId: outputField.id,
        outputField: outputField
      };
    }).filter(select => select !== null);
  }

  protected transformFromFormGroup(formValue: PipelineSelectsForm): Select[] | null {
    return formValue.selects;
  }

  protected getFormControls(): Controls<PipelineSelectsForm> {
    return {
      selects: new FormArray([])
    };
  }

  validate(): ValidationErrors | null {
    if (!this.formGroupControls) {
      return null;
    }

    const valid = this.formGroupControls.selects.controls.map(value => !!value.value.select)
      .reduce((previousValue, currentValue) => previousValue && currentValue, true);

    return valid ? null : { required: true };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.outputFields) {
      const selectsArray = this.formGroupControls.selects;

      clearFormArray(selectsArray);

      this.outputFields.forEach((output, index) => {
        selectsArray.push(
          new FormControl({ ...emptySelect, order: index, outputFieldId: output.id, outputField: output }, Validators.required));
      });

      this.initializePagedSelects();
    }

    if (changes.selectExecutionErrors) {
      this.handleSelectExecutionChanges(changes);
    }
  }

  private initializePagedSelects() {
    this.pagingService = new PipelineSelectsService(this.formGroupControls.selects.controls, this.outputFields, this.keyedSelectErrors);
    this.pagingService.onPage({ length: this.length, pageSize: this.pageSize, pageIndex: this.currentPage, previousPageIndex: 0 });
  }

  private handleSelectExecutionChanges(changes: SimpleChanges) {
    const selectErrors: SelectsExecutionErrors = changes.selectExecutionErrors.currentValue;
    this.keyedSelectErrors = {};
    if (!!selectErrors && !!selectErrors.individualErrors) {
      selectErrors.individualErrors.forEach((selectResult: SelectResult) =>
        this.keyedSelectErrors[selectResult.outputFieldId] = selectResult);
    }

    this.pagingService.fieldsWithErrors = this.keyedSelectErrors;
  }

  attemptAutoFill() {
    const outFieldLookup = new Map<number, Field>();
    this.outputFields.forEach(field => outFieldLookup.set(field.id, field));

    const inFieldLookup = new Map<string, Field>();
    const inFields: Field[] = flatten(this.inputSchemas.map(schema => schema.fields));

    const leftSchemaPhysicalTableNames: SchemaMetadata[] = this.inputSchemasMetadata ? this.inputSchemasMetadata
      .filter(schema => schema.joinSchemaType === 'left') : [];

    const rightSchemaPhysicalTableNames: SchemaMetadata[] = this.inputSchemasMetadata ? this.inputSchemasMetadata
      .filter(schema => schema.joinSchemaType === 'right') : [];

    const aggregationInputSchemaNames: string[] = this.inputSchemasMetadata ? flatten(this.inputSchemasMetadata
      .filter(schema => schema.aggregationSchemaType === 'input')
      .map(schema => schema.physicalTableName)) : [];

    const aggregationGroupingFieldNames: string[] = this.inputSchemasMetadata ? flatten(this.inputSchemasMetadata
      .map(schema => schema.aggregationGroupingFieldNames)) : [];

    inFields.forEach(field => inFieldLookup.set(field.name, field));

    this.formGroupControls.selects.controls.forEach(select => {
      const fieldId = select.value.outputFieldId;
      let fieldName = outFieldLookup.get(fieldId).name;

      if (inFieldLookup.has(fieldName) && !select.value.select) {
        fieldName = this.getSchemaNameFromLeftOrRightSchema(fieldName, leftSchemaPhysicalTableNames, rightSchemaPhysicalTableNames);
        if (aggregationInputSchemaNames.length > 0 &&
          !aggregationGroupingFieldNames.includes(fieldName)) {
          fieldName = "first(".concat(fieldName).concat(")");
        }
        select.setValue({
          ...select.value,
          select: fieldName
        });
      }
    });
  }

  private getSchemaNameFromLeftOrRightSchema(fieldName, leftSchemaPhysicalTableNames: SchemaMetadata[],
                                             rightSchemaPhysicalTableNames: SchemaMetadata[]) {
    if (leftSchemaPhysicalTableNames.length > 0) {
      let schemaIndex = leftSchemaPhysicalTableNames.findIndex(leftSchema => leftSchema.fieldNames.includes(fieldName));
      if (schemaIndex !== -1) {
        fieldName = leftSchemaPhysicalTableNames[schemaIndex].physicalTableName.concat('.', fieldName);
      } else {
        schemaIndex = rightSchemaPhysicalTableNames.findIndex(rightSchema => rightSchema.fieldNames.includes(fieldName));
        if (schemaIndex !== -1) {
          fieldName = rightSchemaPhysicalTableNames[schemaIndex].physicalTableName.concat('.', fieldName);
        }
      }
    }
    return fieldName;
  }

  onPage($event: PageEvent) {
    this.pagingService.onPage($event);
  }

  onFilter(outputFieldName: string) {
    if (outputFieldName !== this.pagingService.currentFilter) {
      this.paginatorComponent.firstPage();
    }
    this.pagingService.onFilter(outputFieldName);
  }

  outputFieldIndex(i: number) {
    return i + this.pagingService.currentPage * this.pagingService.pageSize;
  }

  getOutputField(select: Select) {
    const outputField = this.pagingService.getOutputField(select.outputFieldId);
    return outputField;
  }

  getSelectError(id: number): SelectResult | null {
    return this.keyedSelectErrors[id];
  }

  onShowErrors($event: MatCheckboxChange) {
    this.pagingService.onOnlyShowErrors($event.checked);
  }

  hasSelectErrors() {
    return Object.keys(this.keyedSelectErrors).length > 0;
  }

  onChangeOrder($event: { currentPosition: number; newPosition: number }) {
    const values = this.formGroupControls.selects.value;
    const newArray: Select[] = moveElement<Select>($event.currentPosition, $event.newPosition, values);

    const mapped = newArray.map((value, index) => {
      return {
        ...value,
        order: index
      };
    });

    this.formGroupControls.selects.setValue(mapped);
  }

}
