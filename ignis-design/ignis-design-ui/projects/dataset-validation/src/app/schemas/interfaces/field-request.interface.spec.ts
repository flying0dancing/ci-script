import { FieldRequest } from './field-request.interface';

describe('FieldRequest', () => {
  it('should uppercase name when creating a new instance', () => {
    expect(new FieldRequest(null, '1 field').name).toBe('1 FIELD');
  });

  it('should not uppercase null name when creating a new instance', () => {
    expect(new FieldRequest(null, null).name).toBeNull();
  });
});
