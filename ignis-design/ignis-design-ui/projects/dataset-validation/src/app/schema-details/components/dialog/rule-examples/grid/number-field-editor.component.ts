import { Component } from '@angular/core';
import { AgEditorComponent } from 'ag-grid-angular';
import { Field } from '../../../../../schemas';
import {
  createFieldNotNullableError,
  emptyExampleFieldIfNull
} from '../../../../functions/schema-details.functions';
import { ExampleField } from '../../../../interfaces/rule-example.interface';

@Component({
  selector: 'dv-number-field-editor',
  templateUrl: './number-field-editor.component.html',
  styleUrls: ['./number-field-editor.component.scss']
})
export class NumberFieldEditorComponent implements AgEditorComponent {
  exampleField: ExampleField;
  field: Field;

  agInit(params: any): void {
    this.exampleField = emptyExampleFieldIfNull(params.value);
    this.field = params.field;
  }

  getValue(): ExampleField {
    this.checkNotNullable();

    return this.exampleField;
  }

  private checkNotNullable() {
    if (!this.field.nullable && this.exampleField.value === null) {
      this.exampleField.error = createFieldNotNullableError(this.field.name);
    } else {
      this.exampleField.error = null;
    }
  }
}
