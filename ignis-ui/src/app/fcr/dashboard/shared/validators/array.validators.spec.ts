import { AbstractControl, FormControl } from "@angular/forms";

import { matchValue, minLength, MinMaxValidator } from "./array.validators";

describe("ArrayValidators", () => {
  describe("MinMaxValidator", () => {
    const control: AbstractControl = new FormControl();
    const min = 2;
    const max = 4;

    beforeEach(() => {
      control.setValidators(MinMaxValidator(min, max));
    });

    it("should return `null` when validation is passed", () => {
      control.setValue(["a", "b"]);

      expect(control.errors).toBeNull();
    });

    it("should fail validation when the selected number of options does not meet the minimum limit", () => {
      control.setValue(["a"]);

      const expected = { minOptions: true, min };

      expect(control.errors).toEqual(expected);
    });

    it("should fail validation when the selected number of options exceeds the maximum limit", () => {
      control.setValue(["a", "b", "c", "d", "e"]);

      const expected = { maxOptions: true, max };

      expect(control.errors).toEqual(expected);
    });
  });

  describe("minLength", () => {
    const control: AbstractControl = new FormControl();
    const min = 2;

    beforeEach(() => {
      control.setValidators(minLength(min));
    });

    it("should return `null` when validation is passed", () => {
      control.setValue(["a", "b"]);

      expect(control.errors).toBeNull();
    });

    it("should fail validation when the selected number of options does not meet the minimum limit", () => {
      control.setValue(["a"]);

      const expected = { minLength: true };

      expect(control.errors).toEqual(expected);
    });
  });

  describe("hasValue", () => {
    const control: AbstractControl = new FormControl();

    beforeEach(() => {
      control.setValidators(matchValue(["ab", "ac", "ad"]));
    });

    it("should return `null` when validation is passed", () => {
      control.setValue("ab");

      expect(control.errors).toBeNull();
    });

    it("should fail validation when the value does not match any values in the array", () => {
      control.setValue("az");

      const expected = { matchValue: true };

      expect(control.errors).toEqual(expected);
    });
  });
});
