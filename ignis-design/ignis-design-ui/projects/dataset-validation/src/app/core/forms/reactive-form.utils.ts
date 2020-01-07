import { FormGroup, AbstractControl } from '@angular/forms';

export class ReactiveFormUtilities {
  public static markFormGroupTouched(formGroup: FormGroup) {
    (<any>Object).values(formGroup.controls).forEach(control => {
      control.markAsTouched();

      if (control.controls) {
        control.controls.forEach(c => this.markFormGroupTouched(c));
      }
    });
  }

  public static hasRequiredField(abstractControl: AbstractControl): boolean {
    // form control check
    if (abstractControl.validator) {
      const validator = abstractControl.validator({} as AbstractControl);
      if (validator && validator.required) {
        return true;
      }
    }

    // form group check
    if (abstractControl['controls']) {
      for (const controlName in abstractControl['controls']) {
        if (abstractControl['controls'][controlName]) {
          if (
            ReactiveFormUtilities.hasRequiredField(
              abstractControl['controls'][controlName]
            )
          ) {
            return true;
          }
        }
      }
    }

    return false;
  }
}
