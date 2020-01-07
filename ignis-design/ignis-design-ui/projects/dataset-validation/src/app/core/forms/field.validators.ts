import {
  FormArray,
  FormControl,
  FormGroup,
  ValidationErrors,
  Validators
} from '@angular/forms';

const SIMPLE_NAME_PATTERN = /^[a-zA-Z]+[_\w]*$/;

export class FieldValidators extends Validators {
  public static validateStringMinMax(
    fieldControl: FormControl
  ): ValidationErrors | null {
    if (FieldValidators.fieldType(fieldControl) === 'string') {
      const value: number = fieldControl.value;
      const minLength: number = fieldControl.parent.value['minLength'];
      const maxLength: number = fieldControl.parent.value['maxLength'];

      if (value < 0) {
        console.log('negative');
        return { required: 'Field must be positive' };
      }

      if (maxLength < minLength) {
        return { required: 'Max Length must be greater than Min Length' };
      }
    }

    return null;
  }

  public static validateFormat(
    fieldControl: FormControl
  ): ValidationErrors | null {
    if (
      ['date', 'timestamp'].includes(FieldValidators.fieldType(fieldControl))
    ) {
      const format: string = fieldControl.value;
      return format ? null : { required: 'Date fields require a format' };
    }

    return null;
  }

  public static validateScale(
    fieldControl: FormControl
  ): ValidationErrors | null {
    if (FieldValidators.fieldType(fieldControl) === 'decimal') {
      const scale: number = fieldControl.value;
      const precision: number = fieldControl.parent.value['precision'];

      if (!scale && scale !== 0) {
        return { required: 'Decimal fields require a scale' };
      }

      if (scale < 0) {
        return { negative: 'Scale must be a postive number' };
      }

      if (scale > precision) {
        return { precision: 'Scale cannot be greater than precision' };
      }

      if (scale !== Math.round(scale)) {
        return { required: 'Scale must be an integer' };
      }
      return null;
    }

    return null;
  }

  public static validatePrecision(
    fieldControl: FormControl
  ): ValidationErrors | null {
    if (FieldValidators.fieldType(fieldControl) === 'decimal') {
      const precision: number = fieldControl.value;

      if (!precision && precision !== 0) {
        return { required: 'Decimal fields require a precision' };
      }

      if (precision < 0) {
        return { negative: 'Precision must be a postive number' };
      }

      if (precision !== Math.round(precision)) {
        return { required: 'Precision must be an integer' };
      }
      return null;
    }

    return null;
  }

  private static fieldType(fieldControl: FormControl): string {
    const parent: FormGroup | FormArray = fieldControl.parent;
    if (!parent) {
      return null;
    }
    const type: string = fieldControl.parent.value['type'];
    return type ? type : null;
  }
}

export const FIELD_ERRORS = 'fieldErrors';
