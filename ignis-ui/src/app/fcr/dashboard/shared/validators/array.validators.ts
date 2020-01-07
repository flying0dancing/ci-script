import { AbstractControl, ValidatorFn } from "@angular/forms";

export const MinMaxValidator = (min: number, max: number): ValidatorFn => (
  control: AbstractControl
): { [key: string]: any } =>
  control.value && control.value.length < min
    ? { minOptions: true, min }
    : control.value && control.value.length > max
    ? { maxOptions: true, max }
    : null;

export const minLength = (min: number): ValidatorFn => (
  control: AbstractControl
): { [key: string]: any } =>
  control.value && control.value.length >= min ? null : { minLength: true };

export const matchValue = (matchValues: string[]): ValidatorFn => (
  control: AbstractControl
): { [key: string]: any } =>
  control.value && matchValues.indexOf(control.value) !== -1
    ? null
    : { matchValue: true };
