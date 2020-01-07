import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatSlideToggleChange } from '@angular/material/slide-toggle';
import { ActivatedRoute } from '@angular/router';
import { select, Store } from '@ngrx/store';
import * as d3 from 'd3';
import { Observable, Subscription } from 'rxjs';
import { filter, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { EdgeContext, EdgeKey, NodeContext } from '../../core/dag/dag-context.interface';
import { DagEventService } from '../../core/dag/dag-event.service';
import { InputDagEdge } from '../../core/dag/dag-inputs.interface';
import * as PipelineStepTestsActions from '../../pipeline-step-tests/actions/pipeline-step-tests.actions';
import * as ProductActions from '../../product-configs/actions/product-configs.actions';
import { ProductConfig } from '../../product-configs/interfaces/product-config.interface';
import { getProduct, PRODUCTS_LOADING_STATE } from '../../product-configs/reducers/product-configs.selectors';
import { Schema } from '../../schemas';
import * as PipelineStepActions from '../actions/pipeline-step-form.actions';
import { Reset } from '../actions/pipeline-step-form.actions';
import { GetAll, Update } from '../actions/pipelines.actions';
import { EditPipelineStepEvent } from '../interfaces/events.interfaces';
import { PipelineStep } from '../interfaces/pipeline-step.interface';
import {
  Pipeline,
  PipelineConnectedSets,
  PipelineDisplayError,
  PipelineDisplayReposonseType,
  PipelineEdge,
  PipelineError
} from '../interfaces/pipeline.interface';
import { UpdatePipelineRequest } from '../interfaces/update-pipeline-request.interface';
import * as stepFormSelectors from '../selectors/pipeline-step-form.selector';
import { getPipeline, PIPELINES_LOADING_STATE } from '../selectors/pipelines.selector';
import { PipelineStepEventService } from '../services/pipeline-step-event.service';
import { PipelinesRepository } from '../services/pipelines.repository';
import { Mode, PipelineStepRootFormComponent } from './step-form/pipeline-step-root-form.component';

@Component({
  selector: 'dv-pipeline-details-container',
  templateUrl: './pipeline-details-container.component.html',
  styleUrls: ['./pipeline-details-container.component.scss']
})
export class PipelineDetailsContainerComponent implements OnInit, OnDestroy {
  @ViewChild('pipelineStepForm', { static: true })
  pipelineStepFormComponent: PipelineStepRootFormComponent;

  pipelineId: number;
  pipeline: Pipeline;
  pipelineName: string;
  steps: PipelineStep[];
  pipelineError: PipelineError;
  pipelineDisplayError: { errorMessage: string; cyclicalStep: PipelineStep };

  productId: number;
  productName: string;
  schemas: Schema[] = [];
  pipelineEdges: InputDagEdge[][] = [];

  schemaNodeContext: Map<number, NodeContext> = new Map<number, NodeContext>();
  edgeContextMap: Map<EdgeKey, EdgeContext> = new Map<EdgeKey, EdgeContext>();
  dagZoom = 1.0;
  minDagZoom = 1.0;
  maxDagZoom = 3.0;
  dagColorMapOverride = null;
  showDisconnectedSets = false;

  sidenavOpened: boolean;

  loading$: Observable<boolean> = this.store.select(PIPELINES_LOADING_STATE);
  loaded$: Observable<boolean> = this.loading$.pipe(
    filter(loading => !loading)
  );
  errors$ = this.store.select(stepFormSelectors.ERRORS);
  stepFormLoading$ = this.store.select(stepFormSelectors.LOADING);
  stepFormLoaded$ = this.store.select(stepFormSelectors.LOADED);

  productsLoading$: Observable<boolean> = this.store.select(
    PRODUCTS_LOADING_STATE
  );
  productsLoaded$: Observable<boolean> = this.productsLoading$.pipe(
    filter(loading => !loading)
  );

  idParam$: Observable<number> = this.route.paramMap.pipe(
    map(params => parseInt(params.get('id'), 10))
  );

  productIdParam$: Observable<number> = this.route.paramMap.pipe(
    map(params => parseInt(params.get('productId'), 10))
  );

  pipeline$: Observable<Pipeline> = this.loaded$.pipe(
    withLatestFrom(this.idParam$),
    switchMap(([loaded, id]) => this.store.select(getPipeline(id)))
  );

  product$: Observable<ProductConfig> = this.productsLoaded$.pipe(
    withLatestFrom(this.productIdParam$),
    switchMap(([loaded, id]) => this.store.pipe(select(getProduct(id))))
  );

  editPipelineName = this.dispatchUpdatePipelineName.bind(this);
  private editStepSubscription: Subscription;

  private pipelineStepTestsActionsSubscription: Subscription = this.idParam$
    .pipe(map(id => new PipelineStepTestsActions.GetAll(id)))
    .subscribe(action => this.store.dispatch(action));

  constructor(
    private store: Store<any>,
    private route: ActivatedRoute,
    private stepEventService: PipelineStepEventService,
    private pipelineRepository: PipelinesRepository,
    private dagEventService: DagEventService
  ) {}

  ngOnInit() {
    this.store.dispatch(new ProductActions.GetAll());
    this.store.dispatch(new GetAll());

    this.pipeline$.pipe(filter(pipeline => !!pipeline)).subscribe(pipeline => {
      this.pipelineId = pipeline.id;
      this.pipelineName = pipeline.name;
      this.pipeline = pipeline;
      this.steps = pipeline.steps;
      this.pipelineError = pipeline.error;
      this.retrievePipelineEdges(pipeline);
      this.handlePipelineErrors(pipeline.error);
    });

    this.product$.pipe(filter(product => !!product)).subscribe(product => {
      this.productId = product.id;
      this.productName = product.name;
      this.schemas = product.schemas.sort((a, b) => a.displayName.localeCompare(b.displayName));
      this.createPipelineGraphNodeContext();
    });

    this.dagEventService.edgeClickEvent.subscribe(stepId => {
      this.editStep({ id: stepId });
    });

    this.editStepSubscription = this.stepEventService.editEvent.subscribe(
      (editEvent: EditPipelineStepEvent) => {
        this.editStep(editEvent);
      }
    );
  }

  ngOnDestroy() {
    this.pipelineStepTestsActionsSubscription.unsubscribe();

    this.editStepSubscription.unsubscribe();
  }

  addStep() {
    this.pipelineStepFormComponent.resetForm();
    this.store.dispatch(new Reset());
    this.pipelineStepFormComponent.setMode(Mode.CREATE);
    this.openSidenav();
  }

  editStep(editEvent: EditPipelineStepEvent) {
    this.store.dispatch(new Reset());
    const stepToEdit: PipelineStep = this.steps.find(
      (existingStep: PipelineStep) => existingStep.id === editEvent.id
    );

    this.pipelineStepFormComponent.resetForm();
    if (!this.sidenavOpened) {
      this.pipelineStepFormComponent.writeValue(stepToEdit);
    } else {
      this.closeSidenav();
      this.newWriteValue(editEvent);
    }
    this.pipelineStepFormComponent.setMode(Mode.EDIT);
    this.dagEventService.selectEdge.emit(stepToEdit.id);
    this.openSidenav();
  }

  /*
  * this is just a workaround ,
  * the problem is that "writeValue" doesnt reset to null so in the second call its keeps the old value
  * that's what crushing the UI
  * this bug been raised in the ngx-sub-form and been fixed in the latest version 3.0.2
  * the best solution is to upgrade the ngx-sub-form ,
  * but this will toke some time because we will have to do some changes in each component
  * where we have used the sub-form in it
  * */
  newWriteValue(editEvent) {
    this.store.dispatch(new Reset());
    const stepToEdit: PipelineStep = this.steps.find(
      (existingStep: PipelineStep) => existingStep.id === editEvent.id
    );
    setTimeout(() => this.pipelineStepFormComponent.writeValue(stepToEdit), 0);
  }


  closeSidenav() {
    this.sidenavOpened = false;
    this.dagEventService.selectEdge.emit(null);
    this.pipelineStepFormComponent.resetForm();
  }

  openSidenav() {
    this.sidenavOpened = true;
  }

  saveStep(step: PipelineStep) {
    console.log('saveStep', step);
    this.store.dispatch(new Reset());

    if (step.id) {
      this.store.dispatch(
        new PipelineStepActions.Update(this.pipelineId, step.id, step)
      );
    } else {
      this.store.dispatch(new PipelineStepActions.Post(this.pipelineId, step));
    }
  }

  mousewheel($event: WheelEvent) {
    $event.preventDefault();

    const zoomDirection = Math.sign($event.deltaY);
    if (zoomDirection > 0 && this.dagZoom > this.maxDagZoom) {
      return;
    }

    if (zoomDirection < 0 && this.dagZoom < this.minDagZoom) {
      return;
    }
    this.dagZoom += 0.1 * zoomDirection;
  }

  private dispatchUpdatePipelineName(name: string) {
    this.store.dispatch(
      new Update(this.pipelineId, new UpdatePipelineRequest(name))
    );
  }

  private createPipelineGraphNodeContext() {
    this.schemaNodeContext = new Map<number, NodeContext>();
    for (const schema of this.schemas) {
      this.schemaNodeContext[schema.id] = {
        tooltip: schema.displayName
      };
    }
  }

  private retrievePipelineEdges(pipeline) {
    const edgesSubscription: Subscription = this.pipelineRepository
      .getPipelineEdges(pipeline.id)
      .subscribe((returnType: PipelineConnectedSets | PipelineDisplayError) => {
        switch (returnType.type) {
          case PipelineDisplayReposonseType.SUCCESS:
            const edges = returnType.connectedSets;
            this.pipelineEdges = edges;
            this.extractEdgeContext(edges);
            this.pipelineDisplayError = null;
            return;

          case PipelineDisplayReposonseType.ERROR:
            this.handlePipelineDisplayError(returnType, pipeline);
        }

        edgesSubscription.unsubscribe();
      });
  }

  private extractEdgeContext(edgeSets: PipelineEdge[][]) {
    for (const edgeSet of edgeSets) {
      for (const edge of edgeSet) {
        const key = edge.source + '-' + edge.target;

        this.edgeContextMap[key] = {
          tooltip: `${edge.pipelineStep.type} Step - ${edge.pipelineStep.name}`,
          id: edge.pipelineStep.id
        };
      }
    }
  }

  private handlePipelineErrors(error: PipelineError) {
    if (error && error.graphNotConnected) {
      this.dagColorMapOverride = {};
      const numberOfSets = error.graphNotConnected.connectedSets.length;

      for (let i = 0; i < numberOfSets; i++) {
        const connectedSet = error.graphNotConnected.connectedSets[i];
        const setColor = d3.interpolateMagma(i / numberOfSets);

        for (const schemaNode of connectedSet) {
          this.dagColorMapOverride[schemaNode] = setColor;
        }
      }
    } else {
      this.dagColorMapOverride = null;
    }
  }

  showDisconnectedSetsChange($event: MatSlideToggleChange) {
    this.showDisconnectedSets = $event.checked;
  }

  private findStep(id: number, pipeline: Pipeline): PipelineStep {
    let foundStep = null;
    pipeline.steps.forEach(step => {
      if (step.id === id) {
        foundStep = step;
      }
    });
    return foundStep;
  }

  private handlePipelineDisplayError(
    error: PipelineDisplayError,
    pipeline: Pipeline
  ) {
    this.pipelineEdges = [];
    this.pipelineDisplayError = {
      errorMessage: error.errorMessage,
      cyclicalStep: null
    };

    if (error.cycleError) {
      const offendingStep = this.findStep(
        error.cycleError.cyclicalStep,
        pipeline
      );
      this.pipelineDisplayError = {
        ...this.pipelineDisplayError,
        cyclicalStep: offendingStep
      };
    }
  }
}
