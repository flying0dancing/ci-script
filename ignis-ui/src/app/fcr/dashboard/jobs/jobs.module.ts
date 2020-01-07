import { StagingModule } from "@/core/api/staging/staging.module";
import { DagModule } from "@/core/dag/dag.module";
import { JobRowMenuRendererComponent } from "@/fcr/dashboard/jobs/job-row-menu-renderer.component";
import { JobsHelperService } from "@/fcr/dashboard/jobs/jobs-helper.service";
import { PipelineJobDialogInvocationComponent } from "@/fcr/dashboard/jobs/pipeline/dialog/pipeline-job-dialog-invocation-component";
import { PipelineJobDialogStepComponent } from "@/fcr/dashboard/jobs/pipeline/dialog/pipeline-job-dialog-step.component";
import { PipelineJobDialogComponent } from "@/fcr/dashboard/jobs/pipeline/dialog/pipeline-job-dialog.component";
import { StartPipelineDialogComponent } from "@/fcr/dashboard/jobs/pipeline/start-pipeline-dialog.component";
import { ImportProductJobContainerComponent } from "@/fcr/dashboard/jobs/product/import-product-job-container.component";
import { ProductJobDialogComponent } from "@/fcr/dashboard/jobs/product/product-job-dialog.component";
import { RollbackProductJobContainerComponent } from "@/fcr/dashboard/jobs/product/rollback-product-job-container.component";
import { ValidationStatusRendererComponent } from "@/fcr/dashboard/jobs/renderers/validation-status.renderer";
import { StagingJobDialogDatasetJobsComponent } from "@/fcr/dashboard/jobs/staging/staging-job-dialog-dataset-jobs.component";
import { StagingJobDialogDatasetComponent } from "@/fcr/dashboard/jobs/staging/staging-job-dialog-dataset.component";
import { StopJobDialogComponent } from "@/fcr/dashboard/jobs/stop/stop-job-dialog.component";
import { ValidationJobContainerComponent } from "@/fcr/dashboard/jobs/validation/validation-job-container.component";
import { ValidationJobDialogComponent } from "@/fcr/dashboard/jobs/validation/validation-job-dialog.component";
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';
import { CommonFormsModule } from '@/fcr/shared/forms/common-forms.module';
import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { CommonModule } from "@angular/common";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import { MatListModule } from "@angular/material";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatCheckboxModule } from "@angular/material/checkbox";
import { MatNativeDateModule } from "@angular/material/core";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatMenuModule } from "@angular/material/menu";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatRadioModule } from "@angular/material/radio";
import { MatSelectModule } from "@angular/material/select";
import { MatTooltipModule } from "@angular/material/tooltip";
import { GridModule } from "../shared/grid/grid.module";
import { JobsListComponent } from "./jobs-list.component";
import { JobsComponent } from "./jobs.component";
import { StatusRendererComponent } from "./renderers/status.renderer";
import { StageJobDialogComponent } from "./stage-job-dialog.component";
import { StageJobComponent } from "./stage-job.component";
import { StageJobPipelinesComponent } from "./stage-job-pipelines.component";
import { DownstreamPipelineGraphComponent } from "./downstream-pipeline-graph.component";
import { StagingJobContainerComponent } from "./staging/staging-job-container.component";
import { StagingJobDialogComponent } from "./staging/staging-job-dialog.component";
import { HttpClientModule } from "@angular/common/http";

@NgModule({
  imports: [
    CommonModule,
    GridModule,
    LayoutModule,
    LoadersModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatDialogModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatMenuModule,
    MatNativeDateModule,
    MatRadioModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    MatTooltipModule,
    ReactiveFormsModule,
    StagingModule,
    DagModule,
    HttpClientModule,
    MatListModule,
    CommonFormsModule,
    DateTimeModule
  ],
  exports: [
    ImportProductJobContainerComponent,
    JobsComponent,
    JobsListComponent,
    RollbackProductJobContainerComponent,
    StageJobComponent,
    StageJobPipelinesComponent,
    DownstreamPipelineGraphComponent,
    StagingJobContainerComponent,
    StagingJobDialogDatasetComponent,
    StatusRendererComponent,
    ValidationJobContainerComponent,
    ValidationStatusRendererComponent,
    StopJobDialogComponent,
    StartPipelineDialogComponent,
    PipelineJobDialogComponent,
    StagingJobDialogDatasetJobsComponent
  ],
  declarations: [
    ImportProductJobContainerComponent,
    JobsComponent,
    JobsListComponent,
    JobRowMenuRendererComponent,
    ProductJobDialogComponent,
    RollbackProductJobContainerComponent,
    StageJobComponent,
    StageJobPipelinesComponent,
    DownstreamPipelineGraphComponent,
    StageJobDialogComponent,
    StagingJobContainerComponent,
    StagingJobDialogComponent,
    StagingJobDialogDatasetComponent,
    StatusRendererComponent,
    ValidationJobContainerComponent,
    ValidationJobDialogComponent,
    ValidationStatusRendererComponent,
    StopJobDialogComponent,
    StartPipelineDialogComponent,
    PipelineJobDialogComponent,
    PipelineJobDialogInvocationComponent,
    PipelineJobDialogStepComponent,
    StagingJobDialogDatasetJobsComponent
  ],
  entryComponents: [
    ImportProductJobContainerComponent,
    JobRowMenuRendererComponent,
    RollbackProductJobContainerComponent,
    ProductJobDialogComponent,
    StageJobDialogComponent,
    StagingJobContainerComponent,
    StagingJobDialogComponent,
    StatusRendererComponent,
    ValidationJobContainerComponent,
    ValidationJobDialogComponent,
    ValidationStatusRendererComponent,
    StopJobDialogComponent,
    StartPipelineDialogComponent,
    PipelineJobDialogComponent,
    StagingJobDialogDatasetJobsComponent
  ]
})
export class JobsModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: JobsModule,
      providers: [JobsHelperService]
    };
  }
}
