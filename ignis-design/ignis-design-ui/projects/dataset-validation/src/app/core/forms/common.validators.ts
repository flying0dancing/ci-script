import {
  AbstractControl,
  FormControl,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';

const SIMPLE_NAME_PATTERN = /^[a-zA-Z]+[_\w]*$/;
const NO_SPECIAL_CHARACTERS = /^[a-zA-Z0-9_]*$/;

export class CommonValidators extends Validators {
  public static simpleNamePattern(control: FormControl): ValidationErrors | null {

    if (typeof control.value === 'string') {
      if (!SIMPLE_NAME_PATTERN.test(control.value)) {
        return {
          simpleNamePattern:
            'Must start with a letter and contain only letters, numbers or underscores'
        };
      }
    }
    return null;
  }

  public static fieldNamePattern(control: FormControl): ValidationErrors | null {

    if (typeof control.value === 'string') {
      if (!NO_SPECIAL_CHARACTERS.test(control.value)) {
        return {
          fieldNamePattern:
            'Must contain no special characters'
        };
      }
    }
    return null;
  }

  public static requireMatch(values: string[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const selection = control.value;
      if (typeof selection === 'string' && selection.length > 0) {
        if (
          values
            .map(value => value.toLowerCase())
            .indexOf(selection.toLowerCase()) < 0
        ) {
          return { noMatches: true };
        }
      }
      return null;
    };
  }

  public static requiredTrimmed(control: FormControl): ValidationErrors | null {
    const newControl = {
      ...control,
      value: (control.value || '').trim()
    } as FormControl;

    return Validators.required(newControl);
  }


  public static matchValue = (matchValues: string[]): ValidatorFn => (control: AbstractControl): { [key: string]: any } =>
    control.value && matchValues.indexOf(control.value) !== -1
      ? null
      : { matchValue: true };

  public static escapeRegex(regexString: string): string {
    return regexString.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); // $& means the whole matched string
  }
}
