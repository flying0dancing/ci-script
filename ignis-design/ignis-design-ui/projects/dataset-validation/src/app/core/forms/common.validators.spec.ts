import { AbstractControl, FormControl } from '@angular/forms';
import { CommonValidators } from './common.validators';

describe('CommonValidators', () => {
  let formControl: AbstractControl;

  beforeEach(() => {
    formControl = new FormControl();
  });

  describe('simpleNamePattern', () => {
    beforeEach(() => {
      formControl.setValidators(CommonValidators.simpleNamePattern);
    });

    it('should return null when value type is not a "string" type', () => {
      formControl.setValue(22);

      expect(formControl.errors).toBeNull();
    });

    it('should return null when value matches pattern ^[a-zA-Z]+[_\\w]*$', () => {
      formControl.setValue('VALiD_NAMe');

      expect(formControl.errors).toBeNull();
    });

    it('should return required error when value contains pipe symbol', () => {
      formControl.setValue('V_AL|D_NAM3');

      expect(formControl.errors).toEqual({
        simpleNamePattern:
          'Must start with a letter and contain only letters, numbers or underscores'
      });
    });

    it('should return required error when value contains a space', () => {
      formControl.setValue('IN VALID');

      expect(formControl.errors).toEqual({
        simpleNamePattern:
          'Must start with a letter and contain only letters, numbers or underscores'
      });
    });

    it('should return required error when value starts with number', () => {
      formControl.setValue('1NVALID');

      expect(formControl.errors).toEqual({
        simpleNamePattern:
          'Must start with a letter and contain only letters, numbers or underscores'
      });
    });

    it('should return required error when value has symbols', () => {
      formControl.setValue('A*7^;');

      expect(formControl.errors).toEqual({
        simpleNamePattern:
          'Must start with a letter and contain only letters, numbers or underscores'
      });
    });
  });

  describe('requireMatch', () => {
    it('should return null when value type is not a "string" type', () => {
      formControl.setValidators(CommonValidators.requireMatch(['A', 'B']));

      formControl.setValue(22);

      expect(formControl.errors).toBeNull();
    });

    it('should return null when string input is empty', () => {
      formControl.setValidators(CommonValidators.requireMatch(['A', 'B']));

      formControl.setValue('');

      expect(formControl.errors).toBeNull();
    });

    it('should return null when string input matches', () => {
      formControl.setValidators(CommonValidators.requireMatch(['A', 'B']));

      formControl.setValue('A');

      expect(formControl.errors).toBeNull();

      formControl.setValue('B');

      expect(formControl.errors).toBeNull();
    });

    it('should return null when string input matches case-insensitive', () => {
      formControl.setValidators(CommonValidators.requireMatch(['A', 'B']));

      formControl.setValue('a');

      expect(formControl.errors).toBeNull();

      formControl.setValue('b');

      expect(formControl.errors).toBeNull();
    });

    it('should return no matches error when string input does not match', () => {
      formControl.setValidators(CommonValidators.requireMatch(['A', 'B']));

      formControl.setValue('C');

      expect(formControl.errors).toEqual({ noMatches: true });
    });
  });
});
