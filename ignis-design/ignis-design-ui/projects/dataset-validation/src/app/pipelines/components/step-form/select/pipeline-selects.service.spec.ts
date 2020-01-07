import { AbstractControl, FormControl } from '@angular/forms';
import { Field } from '../../../../schemas';
import { Select } from '../../../interfaces/pipeline-step.interface';
import { PipelineSelectsService } from './pipeline-selects.service';

const emptySelect: Select = {
  outputFieldId: null,
  select: '',
  hasWindow: false,
  order: 0,
  intermediateResult: false,
  window: null
};

const populatedField: Field = {
  id: 1,
  name: '',
  format: '',
  maxLength: 50,
  minLength: 0,
  nullable: true,
  precision: 1,
  scale: 1,
  regularExpression: '',
  type: 'string'
};

function createNFields(n: number, startIndex = 1, name = 'Field ') {
  const fields: Field[] = [];
  for (let i = startIndex; i < (startIndex + n); i++) {
    fields.push({ ...populatedField, id: i, name: name + i });
  }

  return fields;
}


function createNControls(n: number) {
  const controls: AbstractControl[] = [];
  for (let i = 1; i <= n; i++) {
    controls.push(new FormControl({ ...emptySelect, outputFieldId: i, order: i }));
  }

  return controls;
}

describe('PipelineSelectsService', () => {

  describe('onPage', () => {

    it('should page controls', () => {
      const service = new PipelineSelectsService([
        new FormControl({ ...emptySelect, outputFieldId: 1 }),
        new FormControl({ ...emptySelect, outputFieldId: 2 })
      ], [
        { ...populatedField, id: 1, name: 'Field 1' },
        { ...populatedField, id: 2, name: 'Field 2' }
      ], {});

      service.onPage({
        pageSize: 1,
        pageIndex: 0,
        previousPageIndex: null,
        length: 2
      });

      const pagedControls = service.pagedSelectsControls;

      expect(pagedControls.length).toEqual(1);
      expect(pagedControls[0].value.outputFieldId).toEqual(1);
    });

    it('should page 100 controls', () => {
      const service = new PipelineSelectsService(createNControls(100), createNFields(100), {});

      service.onPage({
        pageSize: 12,
        pageIndex: 2,
        previousPageIndex: null,
        length: 100
      });

      const pagedControls = service.pagedSelectsControls;

      expect(pagedControls.length).toEqual(12);
      expect(pagedControls[0].value.outputFieldId).toEqual(25);
      expect(pagedControls[11].value.outputFieldId).toEqual(36);
    });

  });

  describe('onFilter', () => {


    it('should filter controls on any field containing filter', () => {
      const service = new PipelineSelectsService(createNControls(100), createNFields(100), {});

      service.onPage({
        pageSize: 100,
        pageIndex: 0,
        previousPageIndex: null,
        length: 100
      });

      service.onFilter('1');

      const pagedControls = service.pagedSelectsControls;

      expect(pagedControls.length).toEqual(20);
      expect(pagedControls[0].value.outputFieldId).toEqual(1);
      expect(pagedControls[1].value.outputFieldId).toEqual(10);
      expect(pagedControls[2].value.outputFieldId).toEqual(11);
      expect(pagedControls[3].value.outputFieldId).toEqual(12);
      expect(pagedControls[11].value.outputFieldId).toEqual(21);
    });
  });

  describe('onShowErrors', () => {


    it('should only show controls for fields with errors', () => {
      const fieldsWithErrors = {
        4: 'test',
        5: 'test',
        10: 'test',
        27: 'test',
      };
      const service = new PipelineSelectsService(
        createNControls(100), createNFields(100), fieldsWithErrors);

      service.onPage({
        pageSize: 10,
        pageIndex: 0,
        previousPageIndex: null,
        length: 100
      });

      service.onOnlyShowErrors(true);

      const pagedControls = service.pagedSelectsControls;

      expect(pagedControls.length).toEqual(4);
      expect(pagedControls[0].value.outputFieldId).toEqual(4);
      expect(pagedControls[1].value.outputFieldId).toEqual(5);
      expect(pagedControls[2].value.outputFieldId).toEqual(10);
      expect(pagedControls[3].value.outputFieldId).toEqual(27);
    });
  });

  describe('getOutputField', () => {
    it('should return outputField by index in the filtered controls', () => {
      const fields = [];
      fields.push(...createNFields(10, 1, 'United Kingdom'));
      fields.push(...createNFields(80, 11, 'Other'));
      fields.push(...createNFields(10, 91, 'Nike'));
      const service = new PipelineSelectsService(createNControls(100), fields, {});

      service.onPage({
        pageSize: 14,
        pageIndex: 0,
        previousPageIndex: null,
        length: 100
      });

      service.onFilter('Ni');

      const pagedControls = service.pagedSelectsControls;

      expect(pagedControls.length).toEqual(14);

      const pagedFields = [];
      for (let i = 0; i < 13; i++) {
        pagedFields.push(service.getOutputField(service.pagedSelectsControls[i].value.outputFieldId));
      }

      expect(pagedFields.map(field => field.name)).toEqual([
        'United Kingdom1',
        'United Kingdom2',
        'United Kingdom3',
        'United Kingdom4',
        'United Kingdom5',
        'United Kingdom6',
        'United Kingdom7',
        'United Kingdom8',
        'United Kingdom9',
        'United Kingdom10',
        'Nike91',
        'Nike92',
        'Nike93'
      ]);
    });

    function assertControlMatchesField(service, index, name, id) {
      expect(service.getOutputField(id).name).toEqual(name);
      expect(service.pagedSelectsControls[index].value.outputFieldId).toEqual(id);
    }

    it('should return outputField by index for all selects', () => {
      const fields = [];
      fields.push(...createNFields(10, 1, 'United Kingdom'));
      fields.push(...createNFields(80, 11, 'Other'));
      fields.push(...createNFields(10, 91, 'Nike'));
      const service = new PipelineSelectsService(createNControls(100), fields, {});

      service.onPage({
        pageSize: 100,
        pageIndex: 0,
        previousPageIndex: null,
        length: 100
      });

      service.onFilter('4');

      const pagedControls = service.pagedSelectsControls;

      expect(pagedControls.length).toEqual(19);

      assertControlMatchesField(service, 0, 'United Kingdom4', 4);
      assertControlMatchesField(service, 1, 'Other14', 14);
      assertControlMatchesField(service, 2, 'Other24', 24);
      assertControlMatchesField(service, 3, 'Other34', 34);
      assertControlMatchesField(service, 4, 'Other40', 40);
      assertControlMatchesField(service, 5, 'Other41', 41);
      assertControlMatchesField(service, 6, 'Other42', 42);
      assertControlMatchesField(service, 7, 'Other43', 43);
      assertControlMatchesField(service, 8, 'Other44', 44);
      assertControlMatchesField(service, 9, 'Other45', 45);
      assertControlMatchesField(service, 10, 'Other46', 46);
      assertControlMatchesField(service, 11, 'Other47', 47);
      assertControlMatchesField(service, 12, 'Other48', 48);
      assertControlMatchesField(service, 13, 'Other49', 49);
      assertControlMatchesField(service, 14, 'Other54', 54);
      assertControlMatchesField(service, 15, 'Other64', 64);
      assertControlMatchesField(service, 16, 'Other74', 74);
      assertControlMatchesField(service, 17, 'Other84', 84);
      assertControlMatchesField(service, 18, 'Nike94', 94);

    });
  });

  //TODO - Add test for specific HKMA problem
});
