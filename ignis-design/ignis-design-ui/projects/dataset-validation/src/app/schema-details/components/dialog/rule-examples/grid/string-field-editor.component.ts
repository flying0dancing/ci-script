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
  selector: 'dv-string-field-editor',
  templateUrl: './string-field-editor.component.html',
  styleUrls: ['./string-field-editor.component.scss']
})
export class StringFieldEditorComponent implements AgEditorComponent {
  exampleField: ExampleField;
  field: Field;
  notNullableErrorMsg = NOT_NULLABLE_ERROR_MSG;

  agInit(params: any): void {
    this.exampleField = emptyExampleFieldIfNull(params.value);

    this.field = params.field;
  }

  // noinspection JSMethodCanBeStatic
  regularExpressionErrorMessage(pattern: string): string {
    return `must match field regular expression: ${pattern}`;
  }

  minLengthErrorMessage(): string {
    return `min length must be ${this.field.minLength} characters`;
  }

  getValue(): ExampleField {
    this.exampleField.error = null;
    this.checkFieldAttributes();

    return { ...this.exampleField };
  }

  private checkFieldAttributes() {
    const errors: string[] = [
      ...this.checkNotNullable(),
      ...this.checkMinLength(),
      ...this.checkRegularExpression()
    ];
    this.exampleField.error = errors.join(';   ') || null;
  }

  private checkNotNullable(): string[] {
    if (!this.field.nullable && !this.exampleField.value) {
      return [createFieldNotNullableError(this.field.name)];
    } else {
      return [];
    }
  }

  private checkMinLength(): string[] {
    if (this.field.minLength) {
      if (
        !this.exampleField.value ||
        this.exampleField.value.length < this.field.minLength
      ) {
        return [this.minLengthErrorMessage()];
      }
    }
    return [];
  }

  private checkRegularExpression(): string[] {
    const regularExpression = this.field.regularExpression;

    if (regularExpression) {
      const regExp = new RegExp(regularExpression);

      if (!regExp.test(this.exampleField.value)) {
        return [
          this.regularExpressionErrorMessage(this.field.regularExpression)
        ];
      }
    }
    return [];
  }
}
