import { DatasetsActions, DatasetsSelectors, DatasetsTypes } from '@/core/api/datasets';
import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import * as PipelineActions from '@/core/api/pipelines/pipelines.actions';
import { findSchemas, PipelineDownstream, PipelineEdge } from '@/core/api/pipelines/pipelines.interfaces';
import { NAMESPACE as PIPELINES_NAMESPACE } from '@/core/api/pipelines/pipelines.reducer';
import { getPipelineDownstreams } from '@/core/api/pipelines/pipelines.selectors';
import { PipelinesService } from '@/core/api/pipelines/pipelines.service';
import * as StagingActions from '@/core/api/staging/staging.actions';
import { EligiblePipeline, StagingItemRequest } from '@/core/api/staging/staging.interfaces';
import { TablesActions, TablesInterfaces } from '@/core/api/tables';
import { Table } from '@/core/api/tables/tables.interfaces';

import { EdgeContext, EdgeKey, NodeContext } from '@/core/dag/dag-context.interface';
import { DagEventService } from '@/core/dag/dag-event.service';
import { getDatasetCollection } from '@/fcr/dashboard/datasets/datasets.selectors';
import { DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material';
import { Store } from '@ngrx/store';
import * as moment from 'moment';
import { BehaviorSubject, combineLatest, Observable, Subject, Subscription } from 'rxjs';
import { distinctUntilChanged, filter, map, switchMap, tap, withLatestFrom } from 'rxjs/operators';
import { JobsHelperService } from './jobs-helper.service';

import { NAMESPACE } from './jobs.constants';
import * as JobsSelectors from './jobs.selectors';

@Injectable()
export class StageJobFormService {
  private datasetsLoading$: Observable<boolean> = this.store.select(
    JobsSelectors.getDatasetLoading
  );

  private form: FormGroup;

  private items: FormArray;

  private schemas$: Observable<TablesInterfaces.Table[]> = this.store.select(
    JobsSelectors.getSchemas
  );

  private schemasLoading$: Observable<boolean> = this.store.select(
    JobsSelectors.getSchemasLoading
  );
  formLoading$: Observable<boolean> = combineLatest([this.schemasLoading$, this.datasetsLoading$])
    .pipe(
      map(([schemasLoading, datasetsLoading]) => schemasLoading && datasetsLoading));

  private _filteredSchemaSubject$: Subject<Table[]> = new BehaviorSubject([]);
  private _entityCodeValue: Subject<string> = new BehaviorSubject(null);
  private _referenceDateValue: Subject<Date> = new BehaviorSubject(null);

  pipelineDownstreams: PipelineDownstream[] = [];
  private pipelineDownstreamsSubscription: Subscription;
  pipelineDownstreams$: Observable<PipelineDownstream[]> = this.store.select(
    getPipelineDownstreams
  );
  datasets: Dataset[] = [];

  eligiblePipelines: any[];

  private selectedPipelines: BehaviorSubject<EligiblePipeline[]> = new BehaviorSubject([]);

  selectedEligiblePipelines: EligiblePipeline[] = [];
  clickedPipeline: EligiblePipeline;

  selectedPipelineEdges: PipelineEdge[] = [];
  schemaNodeContext: Map<number, NodeContext> = new Map<number, NodeContext>();
  edgeContextMap: Map<EdgeKey, EdgeContext> = new Map<EdgeKey, EdgeContext>();
  isPipelineGraphLoading: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  showPipelines: boolean;

  public sourceFiles$: Observable<DatasetsTypes.SourceFiles> = this.store.select(JobsSelectors.getSourceFiles);

  formReady$: Observable<boolean> = combineLatest([
    this.sourceFiles$,
    this.schemas$
  ]).pipe(
    map(
      ([schemas, sourceFiles]) =>
        !!(schemas && schemas.length && sourceFiles && sourceFiles.length)
    )
  );

  private datePipe = new DatePipe('en-GB');

  private static resetSchemaSelection(formGroup: FormGroup) {
    formGroup.controls.schemaId.reset();
    formGroup.controls.schemaPhysicalName.reset();
    formGroup.controls.schemaVersion.reset();
    formGroup.controls.schemaStartDate.reset();
    formGroup.controls.schemaEndDate.reset();
    formGroup.controls.selectedSchema.reset();
  }

  constructor(
    private fb: FormBuilder,
    private helperService: JobsHelperService,
    private store: Store<any>,
    private http: HttpClient,
    private pipelinesService: PipelinesService,
    private dagEventService: DagEventService
  ) {}

  static toAutocompleteOptions(options: string): string[] {
    return options ? options.split(',') : [];
  }

  init(): FormGroup {
    this.getDatasetsSourceFiles();
    this.getTables();
    this.getAllPipelineDownstreams();

    this.setupPipelineDownstreamsUpdate();
    this.setupDatasetsUpdate();
    this.setupEdgeSelection();
    this.setupShowPipelines();

    this.form = this.fb.group({
      jobName: ['', Validators.required],
      entityCode: [null, Validators.required],
      refDate: [null, Validators.required],
      items: this.fb.array([])
    });

    this.form.controls.entityCode.valueChanges
      .subscribe(entityCode => this._entityCodeValue.next(entityCode));

    this.form.controls.refDate.valueChanges
      .subscribe(refDate => this._referenceDateValue.next(refDate));

    this.items = this.form.get('items') as FormArray;
    this.addItem();

    return this.form;
  }

  getSelectedPipelines(): Observable<EligiblePipeline[]> {
    return this.selectedPipelines.asObservable();
  }

  get filteredSchemas$(): Observable<Table[]> {
    return this._filteredSchemaSubject$.asObservable();
  }

  private getDatasetsSourceFiles(): void {
    this.store.dispatch(
      new DatasetsActions.Empty({ reducerMapKey: NAMESPACE })
    );
    this.store.dispatch(
      new DatasetsActions.GetSourceFiles({ reducerMapKey: NAMESPACE })
    );
  }

  private getTables(): void {
    this.store.dispatch(new TablesActions.Empty({ reducerMapKey: NAMESPACE }));
    this.store.dispatch(new TablesActions.Get({ reducerMapKey: NAMESPACE }));
  }

  private getAllPipelineDownstreams(): void {
    this.store.dispatch(
      new PipelineActions.GetDownstreams({ reducerMapKey: PIPELINES_NAMESPACE })
    );
  }

  createItem(): FormGroup {
    const formGroup = this.fb.group({
      _schemaAutocomplete: [[]],
      _appendDatasetAutocomplete: [[]],
      filePath: [null, Validators.required],
      header: true,
      schemaName: [null, Validators.required],
      selectedSchema: [null, Validators.required],
      schemaId: null,
      schemaPhysicalName: null,
      schemaVersion: null,
      schemaStartDate: null,
      schemaEndDate: null,
      appendToDataset: [null],
      autoValidate: new FormControl({ value: false, disabled: true })
    });
    this.setupDisplayOnlyFields(formGroup);

    this.registerSchemaSelection(formGroup);
    this.registerAppendDatasetAutocomplete(formGroup);
    this.registerDownstreamPipelineAutocomplete(formGroup);

    return formGroup;
  }

  private setupDisplayOnlyFields(formGroup) {
    formGroup.controls.schemaId.disable();
    formGroup.controls.schemaPhysicalName.disable();
    formGroup.controls.schemaVersion.disable();
    formGroup.controls.schemaStartDate.disable();
    formGroup.controls.schemaEndDate.disable();
    formGroup.controls.selectedSchema.enable();

    this.registerRefDateSelection(formGroup);
  }

  private registerRefDateSelection(formGroup: FormGroup) {
    this._referenceDateValue
      .pipe(
        withLatestFrom(this.schemas$),
        tap(([refDate, schemas]) => this._filteredSchemaSubject$.next(this.helperService.filterSchemasByReferenceDate(refDate, schemas))),

        filter(() => !!formGroup.controls.schemaName.value),
        map(([refDate, schemas]) => {
          const schemaName = formGroup.controls.schemaName.value;

          return this.selectSchema(schemaName, refDate, schemas);
        })
      )
      .subscribe((selectedSchema: Table) => {
        if (selectedSchema) {
          this.handleSchemaSelection(formGroup, selectedSchema);
        } else {
          StageJobFormService.resetSchemaSelection(formGroup);
        }
      });
  }

  private registerSchemaSelection(formGroup: FormGroup) {
    formGroup.controls.schemaName.valueChanges
      .pipe(
        withLatestFrom(this.schemas$, this._referenceDateValue),
        map(([schemaName, schemas, refDate]) => {
          return this.selectSchema(schemaName, refDate, schemas);
        })
      )
      .subscribe((selectedSchema: Table) => {
        if (selectedSchema) {
          this.handleSchemaSelection(formGroup, selectedSchema);
        } else {
          StageJobFormService.resetSchemaSelection(formGroup);
        }
      });
  }

  private registerAppendDatasetAutocomplete(formGroup: FormGroup) {
    const appendDatasetAutocomplete =
      formGroup.controls._appendDatasetAutocomplete;

    const schemaIdValue = formGroup.controls.schemaId.valueChanges;

    combineLatest([this._entityCodeValue, this._referenceDateValue, schemaIdValue])
      .pipe(
        distinctUntilChanged(),
        switchMap(([entityCode, refDate, schemaId]) => {
          const refDateString = moment(refDate).format('YYYY-MM-DD');
          return this.store.select(
            DatasetsSelectors.getDatasetsForSchemaIdsEntityCodeAndReferenceDate(
              [schemaId],
              entityCode,
              refDateString
            )
          );
        })
      )
      .subscribe(datasets => appendDatasetAutocomplete.setValue(datasets));
  }

  private selectSchema(
    schemaName: string,
    refDate: Date,
    schemas: Table[]
  ): Table {
    let startsWithMatches: Table[];

    startsWithMatches = this.helperService.filterSchemasByNameAndReferenceDate(
      schemaName,
      refDate,
      schemas
    );

    const equalMatches: Table[] = startsWithMatches
      ? startsWithMatches.filter(schema => schema.displayName === schemaName)
      : [];

    if (equalMatches.length === 1) {
      return equalMatches[0];
    }
    return null;
  }

  public handleSchemaSelection(formGroup: FormGroup, selectedSchema: Table) {
    formGroup.controls.schemaPhysicalName.setValue(
      selectedSchema.physicalTableName
    );
    formGroup.controls.selectedSchema.setValue(
      selectedSchema.physicalTableName
    );
    formGroup.controls.schemaId.setValue(selectedSchema.id);

    const autoValidateControl = formGroup.controls.autoValidate;
    const hasRules = selectedSchema.validationRules.length > 0;
    if (!hasRules) {
      autoValidateControl.setValue(false);
      autoValidateControl.disable({ onlySelf: true });
    } else {
      autoValidateControl.setValue(true);
      autoValidateControl.enable({ onlySelf: true });
    }
    formGroup.controls.schemaVersion.setValue(selectedSchema.version);
    formGroup.controls.schemaStartDate.setValue(
      this.asMediumDate(selectedSchema.startDate)
    );
    formGroup.controls.schemaEndDate.setValue(
      this.asMediumDate(selectedSchema.endDate)
    );
  }

  private registerDownstreamPipelineAutocomplete(formGroup: FormGroup) {
    const appendDatasetAutocomplete =
      formGroup.controls._appendDatasetAutocomplete;

    const schemaIdValue = formGroup.controls.schemaId.valueChanges;

    combineLatest([this._entityCodeValue, this._referenceDateValue, schemaIdValue])
      .pipe(distinctUntilChanged())
      .subscribe(([entityCode, referenceDate, schemaId]) =>
        this.updateEligiblePipelines(entityCode, referenceDate));
  }

  private updateEligiblePipelines(entityCode, referenceDate) {
    const refDateString = referenceDate !== null ? referenceDate.format('YYYY-MM-DD') : null;

    const stagingItemSchemaIds = this.items.controls
      .map((formGroup: FormGroup) => formGroup.controls.schemaId.value);

    this.eligiblePipelines = this.pipelineDownstreams
      .filter(pipelineDownstream =>
        this.downstreamPipelineUsesSchema(pipelineDownstream, stagingItemSchemaIds))
      .map(pipelineDownstream =>
        this.helperService.toEligiblePipeline(
          entityCode, refDateString, stagingItemSchemaIds, this.datasets, pipelineDownstream));
  }

  private downstreamPipelineUsesSchema(
    pipelineDownstream: PipelineDownstream, schemaIds: number[]): boolean {

    for (const requiredSchema of pipelineDownstream.requiredSchemas) {
      if (schemaIds.includes(requiredSchema.id)) {
        return true;
      }
    }
    return false;
  }

  private asMediumDate(date: string) {
    if (!date) {
      return null;
    }
    return this.datePipe.transform(new Date(date), 'mediumDate');
  }

  addItem(): void {
    this.items.push(this.createItem());
  }

  removeItem(index): void {
    this.items.at(index).reset();
    this.items.removeAt(index);
  }

  getSelectedEligiblePipelines($event) {
    this.selectedEligiblePipelines = $event;
  }

  getClickedPipelines($event) {
    this.clickedPipeline = $event;
    this.retrievePipelineEdges(this.clickedPipeline);
  }

  private retrievePipelineEdges(pipeline: EligiblePipeline) {
    this.isPipelineGraphLoading.next(true);
    const edgesSubscription: Subscription = this.pipelinesService
      .getPipelineEdges(pipeline.pipelineId)
      .subscribe((edges: PipelineEdge[]) => {
        this.isPipelineGraphLoading.next(false);
        this.selectedPipelineEdges = edges;
        this.extractEdgeContext(edges, pipeline);
        this.extractSchemasToNodeContext(edges);
        edgesSubscription.unsubscribe();
      });
  }

  private extractSchemasToNodeContext(edges: PipelineEdge[]) {
    for (const edge of edges) {
      const schemas = findSchemas(edge.pipelineStep);
      for (const schema of schemas) {
        this.schemaNodeContext[schema.id] = {
          tooltip: schema.displayName
        };
      }
    }
  }

  private extractEdgeContext(
    edges: PipelineEdge[],
    pipeline: EligiblePipeline
  ) {
    for (const edge of edges) {
      const key = edge.source + '-' + edge.target;

      this.edgeContextMap[key] = {
        tooltip: `${edge.pipelineStep.type} Step - ${edge.pipelineStep.name}`,
        id: edge.pipelineStep.id
      };
    }
  }

  submit() {
    const { jobName, entityCode, refDate, items } = this.form.value;

    const itemsBody = items.map(item => this.createStagingItem(item));

    const downstreamPipelines = this.selectedEligiblePipelines
      .map(selectedPipeline => selectedPipeline.pipelineName);

    this.store.dispatch(
      new StagingActions.StartStagingJob({
        reducerMapKey: NAMESPACE,
        body: {
          name: jobName,
          metadata: {
            entityCode: entityCode,
            referenceDate: this.datePipe.transform(refDate, 'dd/MM/yyyy'),
          },
          items: itemsBody,
          downstreamPipelines: downstreamPipelines
        }
      })
    );
  }

  private createStagingItem(item): StagingItemRequest {
    return {
      source: {
        filePath: item.filePath,
        header: item.header
      },
      autoValidate: item.autoValidate,
      schema: item.schemaName,
      appendToDatasetId: item.appendToDataset ? item.appendToDataset.id : null
    };
  }

  private setupPipelineDownstreamsUpdate() {
    this.pipelineDownstreamsSubscription = this.pipelineDownstreams$
      .pipe(
        filter(
          (pipelineDownstreams: PipelineDownstream[]) =>
            pipelineDownstreams.length > 0
        )
      )
      .subscribe(
        (pipelineDownstreams: PipelineDownstream[]) =>
          (this.pipelineDownstreams = pipelineDownstreams)
      );
  }

  private setupDatasetsUpdate() {
    this.store
      .select(getDatasetCollection)
      .subscribe((datasets: Dataset[]) => (this.datasets = datasets));
  }

  private setupEdgeSelection() {
    this.dagEventService.edgeClickEvent.subscribe(pipeline =>
      this.selectedPipelines.next([pipeline])
    );
  }

  private setupShowPipelines() {
    this.showPipelines = true;
  }

  onShowPipelines($event: MatCheckboxChange) {
    this.showPipelines = $event.checked;
    if (this.showPipelines) {
      this.selectPipelines();
    }
  }

  private selectPipelines() {
    this.selectedPipelines.next(this.selectedEligiblePipelines);
  }
}
