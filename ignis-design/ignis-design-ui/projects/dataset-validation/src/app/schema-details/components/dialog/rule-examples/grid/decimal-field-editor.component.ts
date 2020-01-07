import { Component } from '@angular/core';
import { AgEditorComponent } from 'ag-grid-angular';
import { Field } from '../../../../../schemas';
import {
  createFieldNotNullableError,
  emptyExampleFieldIfNull,
  NOT_NULLABLE_ERROR_MSG
} from '../../../../functions/schema-details.functions';
import { ExampleField } from '../../../../interfaces/rule-example.interface';

@Component({
  selector: 'dv-decimal-field-editor',
  templateUrl: './decimal-field-editor.component.html',
  styleUrls: ['./decimal-field-editor.component.scss']
})
export class DecimalFieldEditorComponent implements AgEditorComponent {
  exampleField: ExampleField;
  field: Field;
  notNullableErrorMsg = NOT_NULLABLE_ERROR_MSG;

  agInit(params: any): void {
    this.exampleField = emptyExampleFieldIfNull(params.value);

    this.field = params.field;
  }

  getValue(): ExampleField {
    this.exampleField.error = null;
    this.checkNotNullable();

    return { ...this.exampleField };
  }

  private checkNotNullable(): void {
    if (!this.field.nullable && !this.exampleField.value) {
      this.exampleField.error = createFieldNotNullableError(this.field.name);
    }
  }
}
