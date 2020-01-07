import * as DatasetActions from '@/core/api/datasets/datasets.actions';
import * as DatasetConstants from '@/core/api/datasets/datasets.constants';
import * as ProductActions from '@/core/api/products/products.actions';
import { NAMESPACE as PRODUCTS_NAMESPACE } from '@/core/api/products/products.reducer';
import { JobStatus, JobType } from '@/core/api/staging/staging.interfaces';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideMockActions } from '@ngrx/effects/testing';
import { StoreModule } from '@ngrx/store';
import * as deepFreeze from 'deep-freeze';
import { of, ReplaySubject, throwError } from 'rxjs';
import * as StagingActions from '../staging.actions';
import { NAMESPACE } from '../staging.constants';
import { StagingEffects } from '../staging.effects';
import { reducer } from '../staging.reducer';
import { StagingService } from '../staging.service';

import { getDatasetsByJobResponse, getJobsResponse, job, jobId, jobs, validationJobRequest } from './staging.mocks';

describe("StagingEffects", () => {
  const reducerMapKey = "test";

  let effects: StagingEffects;
  let actions: ReplaySubject<any>;
  let service: StagingService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        StoreModule.forRoot({
          [NAMESPACE]: reducer
        })
      ],
      providers: [
        StagingEffects,
        StagingService,
        provideMockActions(() => actions)
      ]
    });

    actions = new ReplaySubject();

    effects = TestBed.get(StagingEffects);
    service = TestBed.get(StagingService);
  });

  describe("get$ effect", () => {
    it("should call GET_SUCCESS action", () => {
      spyOn(service, "getJobs").and.returnValue(of(getJobsResponse));

      const action = new StagingActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.get$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetSuccess({ reducerMapKey, jobs })
        );
      });
    });

    it("should call GET_FAIL action", () => {
      spyOn(service, "getJobs").and.returnValue(throwError(undefined));

      const action = new StagingActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.get$.subscribe(result => {
        expect(result).toEqual(new StagingActions.GetFail({ reducerMapKey }));
      });
    });

    it("should not call GET_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "getJobs").and.returnValue(of("staging"));

      const action1 = new StagingActions.Get({ reducerMapKey });
      const action2 = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.get$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("getSilent$ effect", () => {
    it("should call GET_SUCCESS action", () => {
      spyOn(service, "getJobs").and.returnValue(of(getJobsResponse));

      const action = new StagingActions.GetSilent({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getSilent$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetSuccess({ reducerMapKey, jobs })
        );
      });
    });

    it("should call GET_FAIL action", () => {
      spyOn(service, "getJobs").and.returnValue(throwError(undefined));

      const action = new StagingActions.GetSilent({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getSilent$.subscribe(result => {
        expect(result).toEqual(new StagingActions.GetFail({ reducerMapKey }));
      });
    });

    it("should not call GET_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "getJobs").and.returnValue(of("staging"));

      const action1 = new StagingActions.GetSilent({ reducerMapKey });
      const action2 = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.getSilent$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("success$ effect", () => {
    it("should call get datasets and get products actions if job has finished", () => {
      const action1 = new StagingActions.GetRunningJobsSuccess({
        reducerMapKey: reducerMapKey,
        jobs: [job]
      });

      const action2 = new StagingActions.GetRunningJobsSuccess({
        reducerMapKey: reducerMapKey,
        jobs: []
      });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.success$.subscribe(_result => result.push(_result));
      expect(result).toEqual([
        new DatasetActions.GetSilent({
          reducerMapKey: DatasetConstants.NAMESPACE
        }),
        new ProductActions.Get({ reducerMapKey: PRODUCTS_NAMESPACE }),
        new ProductActions.NoImportInProgress({
          reducerMapKey: PRODUCTS_NAMESPACE
        })
      ]);
    });

    it("should not call get datasets and get products actions if no jobs have finished", () => {
      const action1 = new StagingActions.GetRunningJobsSuccess({
        reducerMapKey: reducerMapKey,
        jobs: [job]
      });

      const action2 = new StagingActions.GetRunningJobsSuccess({
        reducerMapKey: reducerMapKey,
        jobs: [job]
      });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.success$.subscribe(_result => result.push(_result));
      expect(result).toEqual([
        new ProductActions.NoImportInProgress({
          reducerMapKey: PRODUCTS_NAMESPACE
        })
      ]);
    });

    it("should not fire no import in progress event if running import jobs", () => {
      const action = new StagingActions.GetSuccess({
        reducerMapKey: reducerMapKey,
        jobs: [
          {
            ...job,
            serviceRequestType: JobType.IMPORT_PRODUCT,
            status: JobStatus.STARTING
          }
        ]
      });

      deepFreeze(action);

      actions.next(action);

      const result = [];

      effects.success$.subscribe(_result => result.push(_result));
      expect(result).toEqual([]);
    });
  });

  describe("startValidationJob$ effect", () => {
    it("should call VALIDATE_SUCCESS action", () => {
      spyOn(service, "startValidationJob").and.returnValue(of(jobId));

      const action = new StagingActions.Validate({
        reducerMapKey,
        body: validationJobRequest
      });

      deepFreeze(action);

      actions.next(action);

      const result = [];

      effects.startValidationJob$.subscribe(_result => result.push(_result));
      expect(result).toEqual([
        new StagingActions.ValidateSuccess({
          reducerMapKey,
          body: jobId,
          datasetId: 123
        }),
        new StagingActions.GetRunningJobs({ reducerMapKey }),
        new DatasetActions.GetSilent({ reducerMapKey: DatasetConstants.NAMESPACE })
      ]);
    });

    it("should call VALIDATE_FAIL action", () => {
      spyOn(service, "startValidationJob").and.returnValue(
        throwError(undefined)
      );

      const action = new StagingActions.Validate({
        reducerMapKey,
        body: validationJobRequest
      });

      deepFreeze(action);

      actions.next(action);

      effects.startValidationJob$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.ValidateFail({ reducerMapKey })
        );
      });
    });

    it("should not call VALIDATE_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "startValidationJob").and.returnValue(of({}));

      const action1 = new StagingActions.Validate({
        reducerMapKey,
        body: validationJobRequest
      });
      const action2 = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.startValidationJob$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("stopJob$ effect", () => {
    it("should call STOP_JOB_SUCCESS action", () => {
      spyOn(service, "stopJob").and.returnValue(of({}));

      const action = new StagingActions.StopJob({
        reducerMapKey,
        jobExecutionId: 1
      });

      deepFreeze(action);

      actions.next(action);

      const result = [];

      effects.stopJob$.subscribe(_result => result.push(_result));
      expect(result).toEqual([
        new StagingActions.StartStagingJobSuccess({ reducerMapKey }),
        new StagingActions.GetRunningJobs({ reducerMapKey })
      ]);
    });

    it("should call STOP_JOB_FAIL action", () => {
      spyOn(service, "stopJob").and.returnValue(throwError(undefined));

      const action = new StagingActions.StopJob({
        reducerMapKey,
        jobExecutionId: 1
      });

      deepFreeze(action);

      actions.next(action);

      effects.stopJob$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.StartStagingJobFail({ reducerMapKey })
        );
      });
    });

    it("should not call STOP_JOB_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "stopJob").and.returnValue(of({}));

      const action1 = new StagingActions.StopJob({
        reducerMapKey,
        jobExecutionId: 1
      });
      const action2 = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.stopJob$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("getJobDatasets$ effect", () => {
    it("should call GET_JOB_DATASETS_SUCCESS action", () => {
      spyOn(service, "getJobDatasets").and.returnValue(
        of(getDatasetsByJobResponse)
      );

      const action = new StagingActions.GetJobDatasets({
        reducerMapKey,
        jobExecutionId: 1
      });

      deepFreeze(action);

      actions.next(action);

      effects.getJobDatasets$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetJobDatasetsSuccess({
            reducerMapKey,
            datasets: getDatasetsByJobResponse
          })
        );
      });
    });

    it("should call GET_JOB_DATASETS_FAIL action", () => {
      spyOn(service, "getJobDatasets").and.returnValue(
        throwError(undefined)
      );

      const action = new StagingActions.GetJobDatasets({
        reducerMapKey,
        jobExecutionId: 1
      });

      deepFreeze(action);

      actions.next(action);

      effects.getJobDatasets$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetJobDatasetsFail({ reducerMapKey })
        );
      });
    });

    it("should not call GET_JOB_DATASETS_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "getJobDatasets").and.returnValue(of({}));

      const action1 = new StagingActions.GetJobDatasets({
        reducerMapKey,
        jobExecutionId: 1
      });
      const action2 = new StagingActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.getJobDatasets$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("getById$ effect", () => {
    it("should call GET_BY_ID_SUCCESS action", () => {
      spyOn(service, "getJob").and.returnValue(
        of(job)
      );

      const action = new StagingActions.GetById({
        reducerMapKey,
        jobId: 1
      });

      deepFreeze(action);

      actions.next(action);

      effects.getById$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetByIdSuccess({
            reducerMapKey,
            job: job
          })
        );
      });
    });

    it("should call GET_BY_ID_FAIL action", () => {
      spyOn(service, "getJob").and.returnValue(
        throwError(undefined)
      );

      const action = new StagingActions.GetById({
        reducerMapKey,
        jobId: 1
      });

      deepFreeze(action);

      actions.next(action);

      effects.getJobDatasets$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetByIdFail({ reducerMapKey })
        );
      });
    });
  });

  describe("getRunningJobs$ effect", () => {
    it("should call GET_RUNNING_JOBS_SUCCESS action", () => {
      spyOn(service, "getJobs").and.returnValue(
        of(getJobsResponse)
      );

      const action = new StagingActions.GetRunningJobs({
        reducerMapKey
      });

      deepFreeze(action);

      actions.next(action);

      effects.getRunningJobs$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetRunningJobsSuccess({
            reducerMapKey,
            jobs: getJobsResponse.data
          })
        );
      });

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

      expect(service.getJobs).toHaveBeenCalledWith(page, search);
    });

    it("should call GET_RUNNING_JOBS_FAIL action", () => {
      spyOn(service, "getJobs").and.returnValue(
        throwError(undefined)
      );

      const action = new StagingActions.GetRunningJobs({
        reducerMapKey
      });

      deepFreeze(action);

      actions.next(action);

      effects.getRunningJobs$.subscribe(result => {
        expect(result).toEqual(
          new StagingActions.GetRunningJobsFail({
            reducerMapKey
          })
        );
      });
    });
  });
});
