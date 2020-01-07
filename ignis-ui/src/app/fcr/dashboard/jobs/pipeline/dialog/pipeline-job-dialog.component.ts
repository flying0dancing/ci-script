import * as PipelineActions from "@/core/api/pipelines/pipelines.actions";
import { PipelineInvocation } from "@/core/api/pipelines/pipelines.interfaces";
import { JobExecution } from "@/core/api/staging/staging.interfaces";
import { NAMESPACE } from "@/fcr/dashboard/jobs/jobs.constants";
import * as JobsSelectors from "@/fcr/dashboard/jobs/jobs.selectors";
import { Component, Inject, OnDestroy, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { Store } from "@ngrx/store";
import { Observable, Subscription } from "rxjs";

@Component({
  selector: "app-pipeline-job-dialog",
  templateUrl: "./pipeline-job-dialog.component.html",
  styleUrls: ["./pipeline-job-dialog.component.scss"]
})
export class PipelineJobDialogComponent implements OnInit, OnDestroy {
  public jobExecution: JobExecution;
  public jobExecutionId: number;

  jobSubscription: Subscription;
  pipelineInvocations$: Observable<PipelineInvocation[]> = this.store.select(
    JobsSelectors.getPipelineInvocations
  );

  constructor(
    public dialogRef: MatDialogRef<PipelineJobDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private store: Store<any>
  ) {}

  ngOnInit(): void {
    this.jobExecutionId = this.data.jobExecutionId;
    this.jobSubscription = this.store
      .select(JobsSelectors.getStagingJobDetailsById(this.data.jobExecutionId))
      .subscribe(jobExecution => {
        this.jobExecution = jobExecution;
        this.store.dispatch(
          new PipelineActions.GetInvocations({
            reducerMapKey: NAMESPACE,
            jobExecutionId: this.data.jobExecutionId
          })
        );
      });
  }

  ngOnDestroy(): void {
    this.jobSubscription.unsubscribe();
  }
}
