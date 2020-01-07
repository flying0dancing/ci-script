import * as DatasetActions from '@/core/api/datasets/datasets.actions';
import * as DatasetConstants from '@/core/api/datasets/datasets.constants';
import * as ProductActions from '@/core/api/products/products.actions';
import { NAMESPACE as PRODUCTS_NAMESPACE } from '@/core/api/products/products.reducer';
import { isRunning, JobExecution, JobType } from '@/core/api/staging/staging.interfaces';
import { Injectable } from '@angular/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { Observable, of } from 'rxjs';
import { catchError, filter, map, mergeMap, pairwise, switchMap, takeUntil } from 'rxjs/operators';
import * as StagingActions from './staging.actions';
import { StagingService } from './staging.service';

@Injectable()
export class StagingEffects {
  @Effect() get$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.Get>(StagingActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.getJobs(action.payload.page).pipe(
        map(res => res.data),
        switchMap((jobs: JobExecution[]) => [
          new StagingActions.GetSuccess({
            reducerMapKey: action.payload.reducerMapKey,
            jobs
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getSilent$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.GetSilent>(StagingActions.GET_SILENT),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.getJobs({}).pipe(
        map(res => res.data),
        map(
          jobs =>
            new StagingActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              jobs
            })
        ),
        catchError(() =>
          of(
            new StagingActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() success$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.GetRunningJobsSuccess>(StagingActions.GET_RUNNING_JOBS_SUCCESS),
    pairwise(),
    map(([previous, current]) => [previous.payload.jobs, current.payload.jobs]),
    mergeMap(([previousJobs, currentJobs]) => {
      const downstreamActions: Action[] = [];

      if (currentJobs.length < previousJobs.length) {
        downstreamActions.push(
          new DatasetActions.GetSilent({
            reducerMapKey: DatasetConstants.NAMESPACE
          }),
          new ProductActions.Get({
            reducerMapKey: PRODUCTS_NAMESPACE
          })
        );
      }

      const runningImportJobs = currentJobs.filter(
        job => job.serviceRequestType === JobType.IMPORT_PRODUCT && isRunning(job)
      );

      if (runningImportJobs.length === 0) {
        downstreamActions.push(
          new ProductActions.NoImportInProgress({
            reducerMapKey: PRODUCTS_NAMESPACE
          })
        );
      }

      return downstreamActions;
    })
  );

  @Effect() getById$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.GetById>(StagingActions.GET_BY_ID),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.getJob(action.payload.jobId).pipe(
        switchMap((job: JobExecution) => [
          new StagingActions.GetByIdSuccess({
            reducerMapKey: action.payload.reducerMapKey,
            job
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.GetByIdFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getRunningJobs$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.GetRunningJobs>(StagingActions.GET_RUNNING_JOBS),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      const page = {sort: {property: 'startTime', direction: 'desc'}};
      const search = {
        expressionType: 'combined',
        operator: 'OR',
        filters: [
          { expressionType: 'simple', columnName: 'status', type: 'equals', filter: 'STARTING' },
          { expressionType: 'simple', columnName: 'status', type: 'equals', filter: 'STARTED' },
          { expressionType: 'simple', columnName: 'status', type: 'equals', filter: 'STOPPING' }
        ]
      };

      return this.service.getJobs(page, search).pipe(
        map(response => response.data),
        switchMap((jobs: JobExecution[]) => [
          new StagingActions.GetRunningJobsSuccess({
            reducerMapKey: action.payload.reducerMapKey,
            jobs: jobs
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.GetRunningJobsFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() startStagingJob$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.StartStagingJob>(StagingActions.START_STAGING_JOB),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.startStagingJob(action.payload.body).pipe(
        mergeMap(() => [
          new StagingActions.StartStagingJobSuccess({
            reducerMapKey: action.payload.reducerMapKey
          }),
          new StagingActions.GetRunningJobs({
            reducerMapKey: action.payload.reducerMapKey
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.StartStagingJobFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() startValidationJob$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.Validate>(StagingActions.VALIDATE),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.startValidationJob(action.payload.body).pipe(
        mergeMap(jobId => [
          new StagingActions.ValidateSuccess({
            reducerMapKey: action.payload.reducerMapKey,
            body: jobId,
            datasetId: action.payload.body.datasetId
          }),
          new StagingActions.GetRunningJobs({
            reducerMapKey: action.payload.reducerMapKey
          }),
          new DatasetActions.GetSilent({
            reducerMapKey: DatasetConstants.NAMESPACE
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.ValidateFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() startPipelineJob$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.StartPipelineJob>(StagingActions.START_PIPELINE_JOB),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.startPipelineJob(action.payload.body).pipe(
        mergeMap(jobId => [
          new StagingActions.StartPipelineJobSuccess({
            reducerMapKey: action.payload.reducerMapKey
          }),
          new StagingActions.GetRunningJobs({
            reducerMapKey: action.payload.reducerMapKey
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.StartPipelineJobFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() stopJob$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.StopJob>(StagingActions.STOP_JOB),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.stopJob(action.payload.jobExecutionId).pipe(
        mergeMap(() => [
          new StagingActions.StartStagingJobSuccess({
            reducerMapKey: action.payload.reducerMapKey
          }),
          new StagingActions.GetRunningJobs({
            reducerMapKey: action.payload.reducerMapKey
          })
        ]),
        catchError(() =>
          of(
            new StagingActions.StartStagingJobFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getJobDatasets$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.GetJobDatasets>(StagingActions.GET_JOB_DATASETS),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service
        .getJobDatasets({
          jobExecutionId: action.payload.jobExecutionId
        })
        .pipe(
          map(
            datasets =>
              new StagingActions.GetJobDatasetsSuccess({
                reducerMapKey: action.payload.reducerMapKey,
                datasets
              })
          ),
          catchError(() =>
            of(
              new StagingActions.GetJobDatasetsFail({
                reducerMapKey: action.payload.reducerMapKey
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  @Effect() getJobDatasetsForDatasetId$: Observable<Action> = this.actions$.pipe(
    ofType<StagingActions.GetJobDatasetsForDataset>(StagingActions.GET_JOB_DATASETS_FOR_DATASET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<StagingActions.Empty>(StagingActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service
        .getJobDatasets({
          datasetId: action.payload.datasetId
        })
        .pipe(
          map(
            datasets =>
              new StagingActions.GetJobDatasetsForDatasetSuccess({
                reducerMapKey: action.payload.reducerMapKey,
                datasets
              })
          ),
          catchError(() =>
            of(
              new StagingActions.GetJobDatasetsForDatasetFail({
                reducerMapKey: action.payload.reducerMapKey
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  constructor(private service: StagingService, private actions$: Actions) {}
}
