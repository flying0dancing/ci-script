import { AbstractControl, Validators } from "@angular/forms";

export const requiredTrimmedValidator = (
  control: AbstractControl
): { [key: string]: any } =>
  typeof control.value === "string" && control.value.trim() === ""
    ? { required: true }
    : Validators.required(control);
