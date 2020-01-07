import { DatasetsActions } from '@/core/api/datasets/';
import { PipelinesService } from '@/core/api/pipelines/pipelines.service';
import { StagingActions } from '@/core/api/staging/';
import { TablesActions } from '@/core/api/tables/';
import { DagEventService } from '@/core/dag/dag-event.service';
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { FormArray, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { StoreModule } from '@ngrx/store';
import { reducers } from '../../_tests/dashboard-reducers.mock';
import { JobsHelperService } from '../jobs-helper.service';

import { NAMESPACE as reducerMapKey } from '../jobs.constants';
import { StageJobFormService } from '../stage-job-form.service';

describe('StageJobFormService', () => {
  let service: StageJobFormService;
  let subscribeSpy: any;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        StoreModule.forRoot(reducers),
        HttpClientTestingModule,
        DateTimeModule,
      ],
      providers: [
        JobsHelperService,
        StageJobFormService,
        HttpClient,
        PipelinesService,
        DagEventService
      ]
    });

    http = TestBed.get(HttpClient);
    service = TestBed.get(StageJobFormService);
    subscribeSpy = jasmine.createSpy('subscribeSpy');

    spyOn((<any>service).store, 'dispatch').and.callThrough();
  });

  describe('formLoading$', () => {
    it('should emit false when `schemasLoading$` or `datasetsLoading$` return falsey values', () => {
      service.formLoading$.subscribe(subscribeSpy);

      expect(subscribeSpy).toHaveBeenCalledTimes(1);
      expect(subscribeSpy).toHaveBeenCalledWith(false);
    });

    it('should emit true when `schemasLoading$` and `datasetsLoading$` return truthy values', () => {
      service.formLoading$.subscribe(subscribeSpy);

      (<any>service).store.dispatch(new TablesActions.Get({ reducerMapKey }));

      expect(subscribeSpy).toHaveBeenCalledWith(false);

      subscribeSpy.calls.reset();

      (<any>service).store.dispatch(
        new DatasetsActions.GetSourceFiles({ reducerMapKey })
      );

      expect(subscribeSpy).toHaveBeenCalledWith(true);
    });
  });

  describe('formReady$', () => {
    it('should emit false when there are no `schemas$` or `sourceFiles$`', () => {
      service.formReady$.subscribe(subscribeSpy);

      expect(subscribeSpy).toHaveBeenCalledTimes(1);
      expect(subscribeSpy).toHaveBeenCalledWith(false);
    });

    it('should emit true when there are `schemas$` and `sourceFiles$`', () => {
      service.formReady$.subscribe(subscribeSpy);

      (<any>service).store.dispatch(
        new TablesActions.GetSuccess({ reducerMapKey, tables: [{}] })
      );

      expect(subscribeSpy).toHaveBeenCalledWith(false);

      subscribeSpy.calls.reset();

      (<any>service).store.dispatch(
        new DatasetsActions.GetSourceFilesSuccess({
          reducerMapKey,
          sourceFiles: ['']
        })
      );

      expect(subscribeSpy).toHaveBeenCalledWith(true);
    });
  });

  describe('addItem', () => {
    it('should add a new form group item to the items array', () => {
      const formGroup = (<any>service).fb.group({});

      spyOn(service, 'createItem').and.returnValue(formGroup);

      (<any>service).items = [];

      service.addItem();

      expect((<any>service).items).toEqual([formGroup]);
    });
  });

  describe('createItem', () => {
    it('should setup display only fields', () => {
      const formControls = service.createItem().controls;

      expect(formControls.schemaPhysicalName.disabled).toBeTruthy();
      expect(formControls.schemaStartDate.disabled).toBeTruthy();
      expect(formControls.schemaEndDate.disabled).toBeTruthy();
      expect(formControls.schemaVersion.disabled).toBeTruthy();
      expect(formControls.selectedSchema.disabled).toBeFalsy();
    });

    it('should return a new form group', () => {
      const item = service.createItem();

      expect(item.constructor).toEqual(FormGroup);
    });
  });

  describe('init', () => {
    beforeEach(() => {
      spyOn(<any>service, 'getDatasetsSourceFiles');
      spyOn(<any>service, 'getTables');

      service.init();
    });

    it('should get the correct data', () => {
      expect((<any>service).getDatasetsSourceFiles).toHaveBeenCalledTimes(1);
      expect((<any>service).getTables).toHaveBeenCalledTimes(1);
    });

    it('should set the form', () => {
      expect((<any>service).form).toBeTruthy();
    });

    it('should set the initial form items', () => {
      expect((<any>service).items).toEqual((<any>service).form.controls.items);
    });
  });

  describe('getAutoCompleteOptions', () => {
    it('should return a string array when options are provided', () => {
      expect(StageJobFormService.toAutocompleteOptions('a,b,c')).toEqual([
        'a',
        'b',
        'c'
      ]);
    });

    it('should return an empty array when no options are provided', () => {
      expect(StageJobFormService.toAutocompleteOptions(null)).toEqual([]);
    });
  });

  describe('removeItem', () => {
    it('should remove a form group item by a given index', () => {
      const formGroup = (<any>service).fb.group({});

      (<any>service).items = new FormArray([formGroup, formGroup]);

      service.removeItem(0);

      expect((<any>service).items.controls).toEqual([formGroup]);
    });
  });

  describe('submit', () => {
    it('should dispatch a post action', () => {
      service.init();

      (<any>service).store.dispatch.calls.reset();

      service.submit();

      expect((<any>service).store.dispatch).toHaveBeenCalledTimes(1);
      expect(
        (<any>service).store.dispatch.calls.mostRecent().args[0].constructor
      ).toEqual(StagingActions.StartStagingJob);
    });
  });

  describe('getDatasetsSourceFiles', () => {
    it('should fire and empty action followed by a get action', () => {
      (<any>service).getDatasetsSourceFiles();

      expect((<any>service).store.dispatch).toHaveBeenCalledTimes(2);
      expect(
        (<any>service).store.dispatch.calls.first().args[0].constructor
      ).toEqual(DatasetsActions.Empty);
      expect(
        (<any>service).store.dispatch.calls.mostRecent().args[0].constructor
      ).toEqual(DatasetsActions.GetSourceFiles);
    });
  });

  describe('getTables', () => {
    it('should fire and empty action followed by a get action', () => {
      (<any>service).getTables();

      expect((<any>service).store.dispatch).toHaveBeenCalledTimes(2);
      expect(
        (<any>service).store.dispatch.calls.first().args[0].constructor
      ).toEqual(TablesActions.Empty);
      expect(
        (<any>service).store.dispatch.calls.mostRecent().args[0].constructor
      ).toEqual(TablesActions.Get);
    });
  });

  describe('registerFileAutoComplete', () => {
    const sourceFiles = ['aa', 'ab', 'abc'];

    let formGroup: FormGroup;

    beforeEach(() => {
      formGroup = (<any>service).fb.group({
        _fileAutocomplete: [[]],
        filePath: ['']
      });

      (<any>service).store.dispatch(
        new DatasetsActions.GetSourceFilesSuccess({
          reducerMapKey,
          sourceFiles
        })
      );

      (<any>service).registerFileAutoComplete(formGroup);
    });

  });

  describe('registerSchemaAutoComplete', () => {
    const schemas = [
      { displayName: 'aa', version: 1 },
      { displayName: 'ab', version: 2 },
      { displayName: 'abc', version: 3 }
    ];

    let formGroup: FormGroup;

    beforeEach(() => {
      formGroup = (<any>service).fb.group({
        _schemaAutocomplete: [[]],
        schemaName: ['']
      });

      (<any>service).store.dispatch(
        new TablesActions.GetSuccess({ reducerMapKey, tables: schemas })
      );

      (<any>service).registerSchemaAutoComplete(formGroup);
    });

  });

  describe('registerSchemaSelection', () => {
    let formGroup: FormGroup;

    beforeEach(() => {
      formGroup = (<any>service).init();

      (<any>service).store.dispatch(
        new TablesActions.GetSuccess({
          reducerMapKey,
          tables: [
            {
              physicalTableName: 'table_one',
              displayName: 'aa',
              validationRules: []
            },
            {
              physicalTableName: 'table_two',
              displayName: 'abc',
              version: 3,
              validationRules: []
            },
            {
              physicalTableName: 'table_2',
              displayName: 'abc',
              version: 33,
              startDate: '2000-01-01',
              endDate: '2001-01-01',
              validationRules: []
            },
            {
              physicalTableName: 'table_three',
              displayName: 'abd',
              startDate: '2000-01-01',
              endDate: '2001-01-01',
              validationRules: [{ id: 1 }]
            },
            {
              physicalTableName: 'table_four',
              displayName: 'az',
              version: 1,
              startDate: '2000-01-01',
              endDate: '2001-01-01',
              validationRules: [{ id: 1 }]
            },
            {
              physicalTableName: 'table_five',
              displayName: 'azz',
              startDate: '2000-01-01',
              endDate: '2001-01-01',
              validationRules: [{ id: 1 }]
            }
          ]
        })
      );
    });

    it('should disable auto validation by default', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const schemaName = firstItemControl.controls.schemaName;
      const autoValidate = firstItemControl.controls.autoValidate;

      expect(autoValidate.disabled).toBeTruthy();

      schemaName.setValue(null);
      schemaName.markAsTouched();

      expect(autoValidate.disabled).toBeTruthy();
    });

    it('should disable auto validation when auto completing', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const schemaName = firstItemControl.controls.schemaName;
      const autoValidate = firstItemControl.controls.autoValidate;

      schemaName.setValue('a');
      schemaName.markAsTouched();

      expect(autoValidate.disabled).toBeTruthy();
      expect(autoValidate.value).toBeFalsy();
    });

    it('should enable auto validation when selecting a schema with rules', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const refDate = formGroup.controls.refDate;
      const schemaName = firstItemControl.controls.schemaName;
      const autoValidate = firstItemControl.controls.autoValidate;

      expect(autoValidate.value).toBeFalsy();

      schemaName.setValue('abd');
      schemaName.markAsTouched();

      refDate.setValue(new Date('2000-02-01'));
      refDate.markAsTouched();

      expect(autoValidate.value).toBeTruthy();
      expect(autoValidate.disabled).toBeFalsy();
    });

    it('should set auto validation to false when selecting a schema with rules', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const schemaName = firstItemControl.controls.schemaName;
      const autoValidate = firstItemControl.controls.autoValidate;

      expect(autoValidate.value).toBeFalsy();

      schemaName.setValue('abc');
      schemaName.markAsTouched();

      expect(autoValidate.value).toBeFalsy();
      expect(autoValidate.disabled).toBeTruthy();
    });

    it('should set physical table name when schema display name is selected', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const refDate = formGroup.controls.refDate;
      const schemaName = firstItemControl.controls.schemaName;
      const schemaPhysicalName = firstItemControl.controls.schemaPhysicalName;

      refDate.setValue(new Date('2000-02-01'));
      refDate.markAsTouched();

      schemaName.setValue(null);
      schemaName.markAsTouched();
      expect(schemaPhysicalName.value).toBeNull();

      schemaName.setValue('az');
      schemaName.markAsTouched();

      expect(schemaPhysicalName.value).toBe('table_four');
    });

    it('should set selected schema name when schema is selected ', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const schemaName = firstItemControl.controls.schemaName;
      const selectedSchema = firstItemControl.controls.selectedSchema;

      expect(selectedSchema.value).toBeNull();

      formGroup.controls.refDate.setValue(new Date('2000-02-01'));

      schemaName.setValue('abc');
      schemaName.markAsTouched();

      expect(selectedSchema.value).toBe('table_2');
    });

    it('should clear physical table name when schema display name is reset', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const refDate = formGroup.controls.refDate;
      const schemaName = firstItemControl.controls.schemaName;
      const schemaPhysicalName = firstItemControl.controls.schemaPhysicalName;

      refDate.setValue(new Date('2000-6-1'));
      refDate.markAsTouched();

      schemaName.setValue('azz');
      schemaName.markAsTouched();

      expect(schemaPhysicalName.value).toBe('table_five');

      schemaName.setValue(null);
      schemaName.markAsTouched();

      expect(schemaPhysicalName.value).toBeNull();
    });

    it('should set schema version ', () => {
      const itemsControl = <FormArray>formGroup.controls.items;
      const firstItemControl = <FormGroup>itemsControl.controls[0];

      const schemaName = firstItemControl.controls.schemaName;
      const schemaVersion = firstItemControl.controls.schemaVersion;

      expect(schemaVersion.value).toBeNull();

      formGroup.controls.refDate.setValue(new Date('2000-02-01'));
      schemaName.setValue('abc');
      schemaName.markAsTouched();

      expect(schemaVersion.value).toBe(33);
    });
  });
});
