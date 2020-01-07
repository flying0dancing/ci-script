import { Component } from '@angular/core';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { AgEditorComponent } from 'ag-grid-angular';
import * as moment from 'moment';
import { ISO_DATE_FORMATS } from '../../../../../core/utilities';
import { Field } from '../../../../../schemas';
import {
  createFieldNotNullableError,
  emptyExampleFieldIfNull
} from '../../../../functions/schema-details.functions';
import { ExampleField } from '../../../../interfaces/rule-example.interface';

@Component({
  selector: 'dv-formatted-date-field-editor',
  templateUrl: './formatted-date-field-editor.component.html',
  styleUrls: ['./formatted-date-field-editor.component.scss'],
  providers: [{ provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMATS }]
})
export class FormattedDateFieldEditorComponent implements AgEditorComponent {
  moment: moment.Moment;
  exampleField: ExampleField;
  field: Field;

  private momentFormat: string;

  private static toMomentDateFormat(javaDateFormat: string) {
    return javaDateFormat
      .replace('dd', 'DD')
      .replace('yyyy', 'YYYY')
      .replace('yy', 'YY')
      .replace(/'(.+?)'/g, '[$&]');
  }

  agInit(params: any): void {
    this.field = params.field;
    this.momentFormat = FormattedDateFieldEditorComponent.toMomentDateFormat(
      this.field.format
    );
    this.exampleField = emptyExampleFieldIfNull(params.value);

    if (this.exampleField.value) {
      this.moment = this.toMoment(this.exampleField.value);
    }
  }

  private toMoment(value: string): moment.Moment {
    return moment(value, this.momentFormat);
  }

  getValue(): ExampleField {
    this.checkNotNullable();

    this.exampleField.value = null;
    if (this.moment) {
      this.exampleField.value = this.moment.format(this.momentFormat);
    }
    return { ...this.exampleField };
  }

  private checkNotNullable() {
    if (!this.field.nullable && !this.moment) {
      this.exampleField.error = createFieldNotNullableError(this.field.name);
    } else {
      this.exampleField.error = null;
    }
  }
}
