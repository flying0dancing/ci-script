import { Component, Inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import * as moment from 'moment';
import { Moment } from 'moment';
import { ISO_DATE_FORMATS } from '../../../../core/utilities';

@Component({
  selector: 'dv-new-schema-version',
  templateUrl: './create-new-version-dialog.component.html',
  styleUrls: ['./create-new-version-dialog.component.scss'],
  providers: [{ provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMATS }]
})
export class CreateNewVersionDialogComponent {
  schemaId: number;
  schemaDisplayName: string;
  minStartDate: moment.Moment;
  minStartDateText: string;

  newVersionForm = this.fb.group({
    startDate: [null, Validators.required]
  });
  startDateControl = this.newVersionForm.controls.startDate;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<CreateNewVersionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data
  ) {
    this.schemaId = data.schemaId;
    this.schemaDisplayName = data.schemaDisplayName;

    this.minStartDate = moment(data.previousStartDate).add(2, 'day');
    this.minStartDateText = moment(data.previousStartDate)
      .add(1, 'day')
      .format('YYYY-MM-DD');
  }

  createNewVersion(): void {
    const startDate: Moment = this.startDateControl.value;

    this.dialogRef.close({
      id: this.schemaId,
      startDate: startDate.format(ISO_DATE_FORMATS.parse.dateInput)
    });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
