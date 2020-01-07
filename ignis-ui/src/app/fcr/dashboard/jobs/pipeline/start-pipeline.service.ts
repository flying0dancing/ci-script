import { DatasetsActions, DatasetsSelectors } from '@/core/api/datasets';
import { NAMESPACE as DATASETS_NAMESPACE } from '@/core/api/datasets/datasets.constants';
import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import * as PipelineActions from '@/core/api/pipelines/pipelines.actions';

import {
  findInputSchemas,
  Pipeline,
  PipelineStep,
  SchemaDetails,
  StartPipelineJobRequest
} from '@/core/api/pipelines/pipelines.interfaces';

import { NAMESPACE as PIPELINE_NAMESPACE } from '@/core/api/pipelines/pipelines.reducer';

import * as PipelinesSelectors from '@/core/api/pipelines/pipelines.selectors';
import * as StagingActions from '@/core/api/staging/staging.actions';
import * as DatasetSelectors from '@/fcr/dashboard/datasets/datasets.selectors';
import { JobsHelperService } from '@/fcr/dashboard/jobs/jobs-helper.service';
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { DatePipe } from '@angular/common';
import { Injectable } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import * as moment from 'moment';
import { combineLatest, Observable, of } from 'rxjs';
import { distinctUntilChanged, map, withLatestFrom } from 'rxjs/operators';
import { ArrayValidators } from '../../shared/validators/index';
import { NAMESPACE as JOBS_NAMESPACE } from './../jobs.constants';

@Injectable()
export class StartPipelineService {
  form: FormGroup;

  private pipelinesLoading$: Observable<boolean> = this.store.select(
    PipelinesSelectors.getPipelinesLoading
  );

  datasetsGetLoading$: Observable<any> = this.store.select(
    DatasetSelectors.getDatasetLoading
  );

  private pipelines$: Observable<Pipeline[]> = this.store.select(
    PipelinesSelectors.getPipelines
  );

  private requiredSchemas$: Observable<SchemaDetails[]> = this.store.select(
    PipelinesSelectors.getRequiredSchemas
  );
  requiredSchemasLoading$: Observable<boolean> = this.store.select(
    PipelinesSelectors.getRequiredSchemasLoading
  );
  requiredSchemasReady$: Observable<boolean> = this.requiredSchemasLoading$.pipe(
    withLatestFrom(this.requiredSchemas$),
    map(([loading, schemas]) => !!schemas && !loading)
  );

  formLoading$: Observable<boolean> = this.pipelinesLoading$ && this.datasetsGetLoading$;
  formReady$: Observable<boolean> = this.pipelines$.pipe(
    map(pipelines => !!pipelines)
  );

  private datePipe = new DatePipe('en-GB');

  private pipelineSteps;

  constructor(
    private fb: FormBuilder,
    private helperService: JobsHelperService,
    private store: Store<any>,
    private localeTime: LocaleTimePipe
  ) {}

  static toAutocompleteOptions(options: string): string[] {
    return options ? options.split(',') : [];
  }

  init(): void {
    this.getDatasets();

    this.form = this.fb.group({
      jobName: ['', Validators.required],
      _pipelineAutocomplete: [[]],
      pipelineName: [null, Validators.required],
      selectedPipeline: [null, Validators.required],
      selectedPipelineStep: [null],
      entityCode: [null, Validators.required],
      referenceDate: [null, Validators.required],
      requiredSchemas: this.fb.array([])
    });

    this.registerPipelineAutoComplete(this.form);
    this.registerPipelineSelection(this.form);
    this.registerPipelineStepSelection(this.form);
    this.registerFormComplete(this.form);
  }

  private getPipelineSchemas(pipelineId: number): void {
    this.store.dispatch(
      new PipelineActions.GetRequiredSchemas({
        reducerMapKey: PIPELINE_NAMESPACE,
        pipelineId: pipelineId
      })
    );
  }

  private getDatasets(): void {
    this.store.dispatch(
      new DatasetsActions.Empty({ reducerMapKey: DATASETS_NAMESPACE })
    );
    this.store.dispatch(
      new DatasetsActions.Get({ reducerMapKey: DATASETS_NAMESPACE })
    );
  }

  private registerPipelineAutoComplete(formGroup: FormGroup) {
    const pipelineAutoCompleteControl =
      formGroup.controls._pipelineAutocomplete;
    const pipelineNameControl = formGroup.controls.pipelineName;

    this.pipelines$.subscribe(pipelines => {
      const pipelineNameOptions = pipelines.map(pipeline => pipeline.name);

      pipelineAutoCompleteControl.setValue(pipelineNameOptions);
      pipelineNameControl.setValidators([
        ArrayValidators.matchValue(pipelineNameOptions)
      ]);
    });

    pipelineNameControl.valueChanges
      .pipe(
        withLatestFrom(this.pipelines$),
        map(([pipelineName, pipelines]) => {
          const selectedPipeline = pipelines.find(pipeline => pipeline.name === pipelineName);
          this.pipelineSteps = selectedPipeline ? selectedPipeline.steps : null;
          return this.helperService.filterPipelines(pipelineName, pipelines);
        })
      )
      .subscribe(filteredPipelines =>
        pipelineAutoCompleteControl.setValue(
          filteredPipelines.map(pipeline => pipeline.name)
        )
      );
  }

  private registerPipelineSelection(formGroup: FormGroup) {
    formGroup.controls.pipelineName.valueChanges
      .pipe(
        withLatestFrom(this.pipelines$),
        map(([pipelineName, pipelines]) =>
          this.selectPipeline(pipelineName, pipelines)
        )
      )
      .subscribe(selectedPipeline => {
        this.handlePipelineSelection(selectedPipeline, formGroup);
      });
  }

  private registerPipelineStepSelection(formGroup: FormGroup) {
    formGroup.controls.selectedPipelineStep.valueChanges
      .pipe(withLatestFrom(this.requiredSchemas$))
      .subscribe(([selectedPipelineStep, schemas]) => {
        this.handlePipelineStepSelection(selectedPipelineStep, schemas, formGroup);
      });
  }

  private selectPipeline(
    pipelineName: string,
    pipelines: Pipeline[]
  ): Pipeline {
    let startsWithMatches: Pipeline[];

    startsWithMatches = pipelines.filter(pipeline => {
      return (
        pipeline.name
          .trim()
          .toLowerCase()
          .indexOf(pipelineName.trim().toLowerCase()) !== -1
      );
    });

    const equalMatches: Pipeline[] = startsWithMatches
      ? startsWithMatches.filter(pipeline => pipeline.name === pipelineName)
      : [];

    if (equalMatches.length === 1) {
      return equalMatches[0];
    }
    return null;
  }

  private handlePipelineSelection(selectedPipeline: any, formGroup: FormGroup) {
    if (selectedPipeline) {
      formGroup.controls.selectedPipeline.setValue(selectedPipeline);
      this.getPipelineSchemas(selectedPipeline.id);
    } else {
      formGroup.controls.selectedPipeline.setValue(null);
      formGroup.setControl('requiredSchemas', this.fb.array([]));
    }
  }

  private handlePipelineStepSelection(selectedStepPipeline: any, schemas, formGroup: FormGroup) {
    formGroup.setControl('requiredSchemas', this.fb.array([]));
    const entityCodeInput = formGroup.controls.entityCode.value;
    const referenceDateInput = formGroup.controls.referenceDate.value;

    return this.getRequiredDatasetsForPipeline(
      schemas,
      entityCodeInput,
      !selectedStepPipeline ? null : selectedStepPipeline,
      referenceDateInput,
      formGroup
    );

  }

  private registerFormComplete(formGroup: FormGroup) {
    const entityCodeInput = formGroup.controls.entityCode.valueChanges;
    const referenceDateInput = formGroup.controls.referenceDate.valueChanges;
    const pipelineStepInput = formGroup.controls.selectedPipelineStep.value;

    combineLatest([this.requiredSchemas$, entityCodeInput, referenceDateInput])
      .pipe(distinctUntilChanged())
      .subscribe(([schemas, entityCode, referenceDate]) => {

        return this.getRequiredDatasetsForPipeline(
          schemas,
          entityCode,
          pipelineStepInput,
          referenceDate,
          formGroup
        );

      });
  }

  private getRequiredDatasetsForPipeline(
    schemas: SchemaDetails[],
    entityCode: string,
    pipelineStep: PipelineStep,
    referenceDate: string,
    formGroup: FormGroup
  ) {
    if (!schemas || !entityCode || !referenceDate) {
      return of([]);
    }
    const refDateString = moment(referenceDate).format('YYYY-MM-DD');
    const requierdSchemas = pipelineStep === null ? schemas : findInputSchemas(pipelineStep);
    const schemaIds = requierdSchemas.map(schema => schema.id);

    this.store
      .select(
        DatasetsSelectors.getDatasetsForSchemaIdsEntityCodeAndReferenceDate(
          schemaIds,
          entityCode,
          refDateString
        )
      )
      .subscribe(datasets =>
        this.updateRequiredDatasetsForm(datasets, requierdSchemas, formGroup)
      );
  }

  private updateRequiredDatasetsForm(
    datasets: Dataset[],
    requiredSchemas: SchemaDetails[],
    formGroup: FormGroup
  ) {
    const schemaIdToMostRecentDataset = {};
    for (const dataset of datasets.sort((d1, d2) => (d2.id > d1.id ? 1 : -1))) {
      if (!schemaIdToMostRecentDataset[dataset.tableId]) {
        schemaIdToMostRecentDataset[dataset.tableId] = dataset;
      }
    }

    formGroup.setControl('requiredSchemas', this.fb.array([]));
    const requiredSchemasControl: FormArray = formGroup.controls
      .requiredSchemas as FormArray;

    for (const requiredSchema of requiredSchemas) {
      const latestDataset = schemaIdToMostRecentDataset[requiredSchema.id];

      const datasetInfo = latestDataset
        ? 'Dataset created on ' +
        this.localeTime.transform(latestDataset.createdTime)
        : null;

      const control: FormGroup = this.fb.group({
        pipelineSchema: [requiredSchema],
        pipelineSchemaDataset: [datasetInfo, Validators.required]
      });

      requiredSchemasControl.push(control);
    }
  }

  submit() {
    const request = this.createPipelineRequest(this.form);

    this.store.dispatch(
      new StagingActions.StartPipelineJob({
        reducerMapKey: JOBS_NAMESPACE,
        body: request
      })
    );
  }

  private createPipelineRequest(formGroup: FormGroup): StartPipelineJobRequest {
    return {
      name: formGroup.controls.jobName.value,
      entityCode: formGroup.controls.entityCode.value,
      referenceDate: this.datePipe.transform(
        formGroup.controls.referenceDate.value,
        'yyyy-MM-dd'
      ),
      pipelineId: formGroup.controls.selectedPipeline.value.id,
      stepId: formGroup.controls.selectedPipelineStep.value
        ? formGroup.controls.selectedPipelineStep.value.id : ''
    };
  }
}
