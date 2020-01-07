import { StagingActions } from '@/core/api/staging';
import { Dataset, JobExecution, JobStatus } from '@/core/api/staging/staging.interfaces';
import { JobDetailsDialogConfig } from '@/fcr/dashboard/jobs/jobs.functions';
import { ChangeDetectionStrategy, Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { NAMESPACE } from '../jobs.constants';
import * as JobsSelectors from '../jobs.selectors';

@Component({
  templateUrl: "./staging-job-container.component.html",
  styleUrls: ['./staging-job-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StagingJobContainerComponent implements OnDestroy, OnInit {
  public stagingGetCollection$: Observable<any> = this.store.select(
    JobsSelectors.getStagingCollection
  );
  public stagingGetDetailsLoading$: Observable<any> = this.store.select(
    JobsSelectors.getStagingLoading
  );
  public stagingGetJobDatasets$: Observable<any> = this.store.select(
    JobsSelectors.getStagingDatasetsDetails
  );
  public getJobByIdLoading$: Observable<boolean> = this.store.select(
    JobsSelectors.getStagingGetByIdLoading
  );

  public datasetJobHistory$: Observable<Dataset[]>;
  public jobExecution$: Observable<JobExecution>;
  private jobExecutionSubscription: Subscription;
  private jobsCollectionSubscription: Subscription;

  selectedJob: boolean;
  displayBackButton: boolean;

  constructor(
    public dialogRef: MatDialogRef<StagingJobContainerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: JobDetailsDialogConfig,
    private store: Store<any>
  ) {}

  public getStagingDatasetsForDatasetId(datasetId) {
    this.dispatchGetJobDatasetsByDatasetIdAction(datasetId);
    this.datasetJobHistory$ = this.store.select(JobsSelectors.getDatasetJobHistory)
      .pipe(
        map(stagingDatasets => stagingDatasets.sort((d1, d2) => d2.id >= d1.id ? 1 : -1))
      );
  }

  public handleDatasetSelection(dataset: Dataset) {
    if (dataset) {
      this.dispatchGetJobByIdAction(dataset.jobExecutionId);
      this.getJobDetails(dataset.jobExecutionId);
      this.selectedJob = true;
      this.displayBackButton = true;
    }
  }

  public handleJobDeselection() {
    this.selectedJob = false;
    this.jobExecutionSubscription.unsubscribe();
  }

  public getJobDetails(jobExecutionId) {
    this.dispatchGetJobDatasetsByJobIdAction(jobExecutionId);

    this.jobExecution$ = this.store.select(
      JobsSelectors.getStagingJobDetailsById(jobExecutionId)
    );
    this.jobExecutionSubscription = this.jobExecution$.subscribe(
      (jobExecution: JobExecution) => {
        if (jobExecution) {
          this.checkAndRegisterJobsSubscription(jobExecution);
        }
      }
    );
  }

  private checkAndRegisterJobsSubscription(jobExecution: JobExecution) {
    if (jobExecution.status === JobStatus.STARTED) {
      // Only subscribe to changes if current details relate to running job

      if (this.jobsCollectionSubscription) {
        this.jobsCollectionSubscription.unsubscribe();
      }

      this.jobsCollectionSubscription = this.stagingGetCollection$.subscribe(
        () => this.dispatchGetJobDatasetsByJobIdAction(jobExecution.id)
      );
    }
  }

  private dispatchGetJobByIdAction(jobExecutionId) {
    this.store.dispatch(
      new StagingActions.GetById({
        reducerMapKey: NAMESPACE,
        jobId: jobExecutionId
      }));
  }

  private dispatchGetJobDatasetsByJobIdAction(jobExecutionId) {
    this.store.dispatch(
      new StagingActions.GetJobDatasets({
        reducerMapKey: NAMESPACE,
        jobExecutionId: jobExecutionId
      })
    );
  }

  private dispatchGetJobDatasetsByDatasetIdAction(datasetId) {
    this.store.dispatch(
      new StagingActions.GetJobDatasetsForDataset({
        reducerMapKey: NAMESPACE,
        datasetId: datasetId
      })
    );
  }

  ngOnDestroy() {
    if (this.jobsCollectionSubscription) {
      this.jobsCollectionSubscription.unsubscribe();
    }
    if (this.jobExecutionSubscription) {
      this.jobExecutionSubscription.unsubscribe();
    }
  }

  ngOnInit(): void {
    if (this.data.datasetId) {
      this.getStagingDatasetsForDatasetId(this.data.datasetId);
    }

    if (this.data.jobExecutionId) {
      this.selectedJob = true;
      this.getJobDetails(this.data.jobExecutionId);
    }
  }
}
