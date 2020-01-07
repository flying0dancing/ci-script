import { StagingActions, StagingInterfaces } from '@/core/api/staging/';
import { JobsListComponent } from '@/fcr/dashboard/jobs/jobs-list.component';
import { StartPipelineDialogComponent } from '@/fcr/dashboard/jobs/pipeline/start-pipeline-dialog.component';
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { filter, pairwise, take } from 'rxjs/operators';
import { NAMESPACE } from '../jobs/jobs.constants';
import { fadeInFactory } from '../shared/animations';
import * as JobsSelectors from './jobs.selectors';
import { StageJobDialogComponent } from './stage-job-dialog.component';

@Component({
  selector: 'app-jobs',
  templateUrl: './jobs.component.html',
  styleUrls: ['./jobs.component.scss'],
  animations: [fadeInFactory(NAMESPACE)]
})
export class JobsComponent implements OnInit {
  runningJobs$: Observable<StagingInterfaces.JobExecution[]> = this.store.select(
    JobsSelectors.getStagingRunningJobs
  );

  private stageJobDialogRef: MatDialogRef<StageJobDialogComponent>;
  private pipelineJobDialogRef: MatDialogRef<StartPipelineDialogComponent>;
  private stagingJobsState$: Observable<any> = this.store.select(
    JobsSelectors.getStagingJobState
  );
  private pipelineJobsState$: Observable<any> = this.store.select(
    JobsSelectors.getPipelineJobsState
  );
  private successfulStageJobs$ = this.stagingJobsState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev.loading && !curr.loading && !curr.error)
  );
  private successfulPipelineJobs$ = this.pipelineJobsState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev.loading && !curr.loading && !curr.error)
  );

  private stageJobsDialogConfig: MatDialogConfig = {
    maxWidth: '97vw',
    width: '97vw',
    height: '97vh',
    maxHeight: '97vh'
  };

  private pipelineJobsDialogConfig: MatDialogConfig = {
    maxWidth: '50vw',
    width: '50vw'
  };

  private featureChangeSubscription: Subscription;

  private refreshTimeout;

  @ViewChild('jobsList', { static: true }) jobsListComponent: JobsListComponent;

  constructor(private store: Store<any>, private dialog: MatDialog) {
    this.registerStageJobSubscriptions();
  }

  private registerStageJobSubscriptions() {
    this.successfulStageJobs$.subscribe(
      this.handleCloseStagingJobsDialog.bind(this)
    );
    this.successfulPipelineJobs$.subscribe(
      this.handleClosePipelineJobsDialog.bind(this)
    );
    this.runningJobs$
      .pipe(pairwise())
      .subscribe(
        this.refreshRunningJobs.bind(this)
      );
  }

  private handleCloseStagingJobsDialog() {
    if (this.stageJobDialogRef) {
      this.stageJobDialogRef.close();
    }
  }

  private handleClosePipelineJobsDialog() {
    if (this.pipelineJobDialogRef) {
      this.pipelineJobDialogRef.close();
    }
  }

  private refreshRunningJobs([previousJobs, currentJobs]) {
    const previousCount = previousJobs.length;
    const currentCount = currentJobs.length;

    if (currentCount < previousCount) {
      this.jobsListComponent.resetGrid();
    }

    if (currentCount > 0) {
      if (this.refreshTimeout) {
        clearTimeout(this.refreshTimeout);
      }

      this.refreshTimeout = setTimeout(() => {
        this.store.dispatch(new StagingActions.GetRunningJobs({ reducerMapKey: NAMESPACE }));
      }, 10000);
    }
  }

  ngOnInit() {
    this.store.dispatch(new StagingActions.GetRunningJobs({ reducerMapKey: NAMESPACE }));

  }

  public handleAddJobButtonClick() {
    this.stageJobDialogRef = this.dialog.open(
      StageJobDialogComponent,
      this.stageJobsDialogConfig
    );

    this.stageJobDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (this.stageJobDialogRef = undefined));
  }

  public handlePipelineJobButtonClick() {
    this.pipelineJobDialogRef = this.dialog.open(
      StartPipelineDialogComponent,
      this.pipelineJobsDialogConfig
    );

    this.pipelineJobDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (this.pipelineJobDialogRef = undefined));
  }

  public handleRefreshJobsButtonClick() {
    this.store.dispatch(new StagingActions.GetRunningJobs({ reducerMapKey: NAMESPACE }));
    this.jobsListComponent.resetGrid();
  }
}
