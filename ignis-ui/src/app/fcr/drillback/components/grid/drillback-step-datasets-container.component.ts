import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import { PipelineStep, PipelineStepInvocation } from '@/core/api/pipelines/pipelines.interfaces';
import { DatasetSchema, InputFieldToOutputField } from '@/fcr/drillback/drillback.interfaces';
import { ChangeDetectionStrategy, Component, Input, OnChanges, OnInit } from '@angular/core';

export class DatasetAndSchema {
  dataset: Dataset;
  datasetSchema: DatasetSchema;
}

@Component({
  selector: "app-drillback-step-datasets",
  templateUrl: "./drillback-step-datasets-container.component.html",
  styleUrls: ["./drillback-step-datasets-container.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DrillbackStepDatasetsContainerComponent
  implements OnInit, OnChanges {
  @Input()
  inputSchemaIdToSchema: Map<number, DatasetSchema>;
  @Input()
  inputDatasets: Dataset[];

  @Input()
  outputSchemaIdToSchema: Map<number, DatasetSchema>;
  @Input()
  outputDatasets: Dataset[];

  @Input()
  pipelineId: number;
  @Input()
  pipelineStep: PipelineStep;
  @Input()
  pipelineStepInvocation: PipelineStepInvocation;

  @Input()
  schemaToDrillbackMapping: Map<string, InputFieldToOutputField>;

  inputDatasetsAndSchema: DatasetAndSchema[] = [];
  outputDatasetsAndSchema: DatasetAndSchema[] = [];

  ngOnInit(): void {
    this.refreshDatasetsAndSchemas();
  }

  ngOnChanges(): void {
    this.refreshDatasetsAndSchemas();
  }

  private refreshDatasetsAndSchemas(): void {
    if (this.inputSchemaIdToSchema && this.inputDatasets) {
      this.inputDatasetsAndSchema = [];

      this.inputDatasets.forEach((dataset: Dataset) => {
        const datasetSchema = this.inputSchemaIdToSchema[dataset.tableId];
        this.inputDatasetsAndSchema.push({
          dataset: dataset,
          datasetSchema: datasetSchema
        });
      });
      this.inputDatasetsAndSchema.sort(this.alphabeticalDatasetOrderSort);
    }
    if (this.outputSchemaIdToSchema && this.outputDatasets) {
      this.outputDatasetsAndSchema = [];

      this.outputDatasets.forEach((dataset: Dataset) => {
        const datasetSchema = this.outputSchemaIdToSchema[dataset.tableId];
        this.outputDatasetsAndSchema.push({
          dataset: dataset,
          datasetSchema: datasetSchema
        });
      });

      this.outputDatasetsAndSchema.sort(this.alphabeticalDatasetOrderSort);
    }
  }

  private alphabeticalDatasetOrderSort(
    a: DatasetAndSchema,
    b: DatasetAndSchema
  ): number {
    if (a.dataset.table > b.dataset.table) {
      return 1;
    } else if (a.dataset.table < b.dataset.table) {
      return -1;
    } else {
      return 0;
    }
  }

  isInputDatasetOutdated(datasetAndSchema: DatasetAndSchema): boolean {
    const pipelineInputDataset = this.pipelineStepInvocation.datasetsIn
      .find(dataset => dataset.datasetId === datasetAndSchema.dataset.id);

    if (pipelineInputDataset) {
      const latestDatasetRunKey = datasetAndSchema.dataset.runKey;
      return latestDatasetRunKey > pipelineInputDataset.datasetRunKey;
    }
    return false;
  }
}
