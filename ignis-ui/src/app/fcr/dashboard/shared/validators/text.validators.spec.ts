import { AbstractControl, FormControl } from "@angular/forms";

import * as TextValidators from "./text.validators";

describe("TextValidators", () => {
  describe("requiredTrimmedValidator", () => {
    const control: AbstractControl = new FormControl();
    const min = 2;

    beforeEach(() => {
      control.setValidators(TextValidators.requiredTrimmedValidator);
    });

    it("should return `null` when value is not a string", () => {
      control.setValue(1);

      expect(control.errors).toBeNull();

      control.setValue([1, 2]);

      expect(control.errors).toBeNull();

      control.setValue({ a: "test" });

      expect(control.errors).toBeNull();
    });

    it("should fail validation when string length (after trimmed) is 0", () => {
      control.setValue(" ");

      expect(control.errors).toEqual({ required: true });
    });
  });
});
