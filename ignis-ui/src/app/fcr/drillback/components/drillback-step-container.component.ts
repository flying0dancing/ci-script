import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import { PipelineInvocation, PipelineStepInvocation } from '@/core/api/pipelines/pipelines.interfaces';
import { getDatasetByIds } from '@/fcr/dashboard/datasets/datasets.selectors';
import { DrillbackStepDetails } from '@/fcr/drillback/drillback.interfaces';
import { DrillbackService } from '@/fcr/drillback/drillback.service';
import { Component, Input, OnChanges, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';

@Component({
  selector: "app-drillback-step-container",
  templateUrl: "./drillback-step-container.component.html",
  providers: [DrillbackService]
})
export class DrillbackStepContainerComponent implements OnInit, OnChanges {
  @Input()
  private pipelineId: number;
  @Input()
  private pipelineInvocation: PipelineInvocation;
  @Input()
  pipelineStepInvocation: PipelineStepInvocation;

  drillBackDetails: DrillbackStepDetails;
  inputDatasets: Dataset[] = [];
  outputDatasets: Dataset[] = [];

  constructor(
    private drillBackService: DrillbackService,
    private store: Store<any>
  ) {}

  ngOnInit(): void {
    this.refreshDrillback();
  }

  ngOnChanges(): void {
    this.refreshDrillback();
  }

  private refreshDrillback(): void {
    this.drillBackService
      .getDrillbackColumnsForStep(
        this.pipelineId,
        this.pipelineStepInvocation.pipelineStep.id
      )
      .subscribe(
        drillBackDetails => (this.drillBackDetails = drillBackDetails)
      );

    this.store
      .select(
        getDatasetByIds(
          this.findStepInvocationInputDatasets(
            this.pipelineInvocation,
            this.pipelineStepInvocation
          )
        )
      )
      .subscribe(datasets => (this.inputDatasets = datasets));

    this.store
      .select(getDatasetByIds([this.pipelineStepInvocation.datasetOutId]))
      .subscribe(datasets => (this.outputDatasets = datasets));
  }

  private findStepInvocationInputDatasets(
    invocation: PipelineInvocation,
    stepInvocation: PipelineStepInvocation
  ): number[] {
    return invocation.invocationSteps
      .filter(step =>
        stepInvocation.inputPipelineStepIds.includes(step.pipelineStep.id)
      )
      .map(step => step.datasetOutId)
      .concat(stepInvocation.datasetsIn.map(dataset => dataset.datasetId));
  }
}
