import { Component } from '@angular/core';
import { AgEditorComponent } from 'ag-grid-angular';
import { Field } from '../../../../../schemas';
import {
  createFieldNotNullableError,
  emptyExampleFieldIfNull
} from '../../../../functions/schema-details.functions';
import { ExampleField } from '../../../../interfaces/rule-example.interface';

@Component({
  selector: 'dv-boolean-field-editor',
  templateUrl: './boolean-field-editor.component.html',
  styleUrls: ['./boolean-field-editor.component.scss']
})
export class BooleanFieldEditorComponent implements AgEditorComponent {
  exampleField: ExampleField;

  private field: Field;

  agInit(params: any): void {
    this.exampleField = emptyExampleFieldIfNull(params.value);
    this.field = params.field;
  }

  getValue(): ExampleField {
    this.checkNotNullable();

    return this.exampleField;
  }

  private checkNotNullable() {
    if (!this.field.nullable && !this.exampleField.value) {
      this.exampleField.error = createFieldNotNullableError(this.field.name);
    } else {
      this.exampleField.error = null;
    }
  }
}
