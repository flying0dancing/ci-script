import { ChangeDetectionStrategy, Component, Inject } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Moment } from 'moment';
import { CommonValidators } from '../../../core/forms/common.validators';

import { FieldValidators } from '../../../core/forms/field.validators';
import { ReactiveFormUtilities } from '../../../core/forms/reactive-form.utils';
import { ISO_DATE_FORMATS } from '../../../core/utilities';
import { CREATE_REQUEST, CreateSchemaRequest } from '../../interfaces/create-schema-request.interface';

export enum Mode {
  CREATE = 'CREATE',
  COPY = 'COPY'
}

@Component({
  selector: 'dv-add-schema-dialog',
  templateUrl: './create-schema-dialog.component.html',
  styleUrls: ['./create-schema-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{ provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMATS }]
})
export class CreateSchemaDialogComponent {

  schemaForm = this.fb.group({
    physicalTableName: [null, CommonValidators.simpleNamePattern],
    displayName: [null, FieldValidators.required],
    startDate: [null, Validators.required],
    endDate: null
  });
  schemaPhysicalTableNameControl = this.schemaForm.get('physicalTableName');
  schemaDisplayNameControl = this.schemaForm.get('displayName');
  schemaStartDateControl = this.schemaForm.get('startDate');
  schemaEndDateControl = this.schemaForm.get('endDate');
  fields: FormArray = <FormArray>this.schemaForm.get('fields');

  confirmed = false;
  mode: Mode = Mode.CREATE;

  constructor(
    private fb: FormBuilder,
    private dialogRef: MatDialogRef<CreateSchemaDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data
  ) {
    this.mode = data.mode ? data.mode : Mode.CREATE;
  }

  dialogTitle() {
    if (this.mode === Mode.COPY) {
      return 'Copy';
    } else {
      return 'Create';
    }
  }

  saveSchema(): void {
    this.confirmed = true;
    ReactiveFormUtilities.markFormGroupTouched(this.schemaForm);

    const request: CreateSchemaRequest = this.buildSchemaRequest();

    this.data = {
      ...this.data,
      schema: request,
      mode: this.mode
    };
    if (this.schemaForm.valid) {
      this.dialogRef.close(this.data);
    }
  }

  cancel(): void {
    this.dialogRef.close();
  }

  buildSchemaRequest(): CreateSchemaRequest {
    const physicalTableName: string = this.schemaPhysicalTableNameControl.value;
    const displayName: string = this.schemaDisplayNameControl.value;
    const startDate: Moment = this.schemaStartDateControl.value;
    const endDate: Moment = this.schemaEndDateControl.value;

    return {
      physicalTableName: physicalTableName.toUpperCase(),
      displayName: displayName,
      startDate: startDate.format(ISO_DATE_FORMATS.parse.dateInput),
      endDate: endDate
        ? endDate.format(ISO_DATE_FORMATS.parse.dateInput)
        : null,
      majorVersion: 1,
      fields: [],
      type: CREATE_REQUEST,
      validationRules: []
    };
  }

}
