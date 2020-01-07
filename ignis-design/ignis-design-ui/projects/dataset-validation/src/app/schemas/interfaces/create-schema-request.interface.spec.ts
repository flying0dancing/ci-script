import { CreateSchemaRequest } from './create-schema-request.interface';

describe('CreateSchemaRequest', () => {
  it('should uppercase name when creating a new instance', () => {
    expect(
      new CreateSchemaRequest('2001-01-01', 'My_scHema').physicalTableName
    ).toBe('MY_SCHEMA');
  });

  it('should not uppercase null name when creating a new instance', () => {
    expect(
      new CreateSchemaRequest('2001-01-01', null).physicalTableName
    ).toBeNull();
  });
});
