import * as DatasetActions from "@/core/api/datasets/datasets.actions";
import { NAMESPACE as DATASETS_NAMESPACE } from "@/core/api/datasets/datasets.constants";
import * as PipelineActions from "@/core/api/pipelines/pipelines.actions";
import {
  Pipeline,
  PipelineInvocation,
  PipelineStepInvocation
} from "@/core/api/pipelines/pipelines.interfaces";
import { NAMESPACE as PIPELINES_NAMESPACE } from "@/core/api/pipelines/pipelines.reducer";
import {
  getPipelineInvocations,
  getPipelineInvocationsLoading,
  getPipelines
} from "@/core/api/pipelines/pipelines.selectors";
import { PipelineInvocationSelectEvent } from "@/fcr/drillback/drillback.interfaces";
import { PipelineSelectService } from "@/fcr/drillback/pipeline-select.service";
import { Component, OnDestroy, OnInit } from "@angular/core";
import { FormBuilder } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { Store } from "@ngrx/store";
import { Observable, Subscription } from "rxjs";
import { filter, map } from "rxjs/operators";

@Component({
  selector: "app-drillback",
  templateUrl: "./drillback.component.html",
  styleUrls: ["./drillback.component.scss"]
})
export class DrillBackComponent implements OnInit, OnDestroy {
  selectedFirstPipeline = false;
  pipelineInvocationId: number;
  pipelineStepInvocationId: number;

  pipelineInvocations: PipelineInvocation[] = [];
  pipelines: Pipeline[] = [];
  selectedPipelineInvocation: PipelineInvocation = null;
  selectedPipelineStepInvocation: PipelineStepInvocation = null;

  private pipelineInvocationsSubscription: Subscription;
  private pipelinesSubscription: Subscription;

  private pipelineInvocationIdParamSubscription: Subscription;
  private pipelineStepInvocationIdParamSubscription: Subscription;

  pipelineInvocationsLoading$ = this.store.select(
    getPipelineInvocationsLoading
  );
  private pipelineInvocations$: Observable<
    PipelineInvocation[]
  > = this.store.select(getPipelineInvocations);

  constructor(
    private router: Router,
    private activeRoute: ActivatedRoute,
    private formBuilder: FormBuilder,
    private store: Store<any>,
    private pipelineSelectService: PipelineSelectService
  ) {}

  ngOnInit(): void {
    this.getAllPipelines();
    this.getAllPipelineInvocations();
    this.getAllDatasets();

    this.setupPipelinesUpdate();
    this.setupPipelinesUpdate();
    this.setupPipelineInvocationsUpdate();
    this.setupPipelineInvocationSelection();

    this.setupPipelineInvocationIdQueryParamUpdate();
  }

  private getAllDatasets(): void {
    this.store.dispatch(
      new DatasetActions.Get({ reducerMapKey: DATASETS_NAMESPACE })
    );
  }

  private getAllPipelines(): void {
    this.store.dispatch(
      new PipelineActions.Get({ reducerMapKey: PIPELINES_NAMESPACE })
    );
  }

  private getAllPipelineInvocations(): void {
    this.store.dispatch(
      new PipelineActions.GetInvocations({ reducerMapKey: PIPELINES_NAMESPACE })
    );
  }

  private setupPipelineInvocationsUpdate(): void {
    this.pipelineInvocationsSubscription = this.pipelineInvocations$
      .pipe(
        filter(
          (pipelineInvocations: PipelineInvocation[]) =>
            pipelineInvocations.length > 0
        )
      )
      .subscribe(
        (pipelineInvocations: PipelineInvocation[]) =>
          (this.pipelineInvocations = pipelineInvocations)
      );
  }

  private setupPipelinesUpdate(): void {
    this.pipelinesSubscription = this.store
      .select(getPipelines)
      .pipe(filter((pipelines: Pipeline[]) => pipelines.length > 0))
      .subscribe((pipelines: Pipeline[]) => (this.pipelines = pipelines));
  }

  private setupPipelineInvocationIdQueryParamUpdate(): void {
    this.pipelineInvocationIdParamSubscription = this.findQueryParam(
      "pipelineInvocationId"
    ).subscribe(
      (pipelineInvocationId: string) =>
        (this.pipelineInvocationId = parseInt(pipelineInvocationId, 10))
    );

    this.pipelineStepInvocationIdParamSubscription = this.findQueryParam(
      "pipelineStepInvocationId"
    ).subscribe(
      (stepInvocationId: string) =>
        (this.pipelineStepInvocationId = parseInt(stepInvocationId, 10))
    );
  }

  private findQueryParam(paramName: string): Observable<string> {
    return this.activeRoute.queryParamMap.pipe(
      filter(queryParamMap => queryParamMap.has(paramName)),
      map(queryParamMap => queryParamMap.get(paramName))
    );
  }

  private setupPipelineInvocationSelection(): void {
    this.pipelineSelectService.pipelineInvocationSelectEvent.subscribe(
      (event: PipelineInvocationSelectEvent) => {
        this.selectedPipelineInvocation = event.pipelineInvocation;
        this.selectedPipelineStepInvocation = event.pipelineStepInvocation;

        if (event) {
          this.selectedFirstPipeline = true;
          this.router.navigate(["drillback"], {
            queryParams: {
              pipelineInvocationId: this.selectedPipelineInvocation.id,
              pipelineStepInvocationId: this.selectedPipelineStepInvocation.id
            },
            queryParamsHandling: "merge"
          });
        }
      }
    );
  }

  ngOnDestroy(): void {
    this.pipelineInvocationsSubscription.unsubscribe();
    this.pipelinesSubscription.unsubscribe();
    this.pipelineInvocationIdParamSubscription.unsubscribe();
    this.pipelineStepInvocationIdParamSubscription.unsubscribe();
  }
}
