import { ExitStatus, JobExecution, JobStatus, JobType } from '@/core/api/staging/staging.interfaces';
import { NAMESPACE } from '../staging.constants';
import { initialStagingState, StagingState } from '../staging.reducer';
import * as StagingSelectors from '../staging.selectors';

const populatedJob: JobExecution = {
  name: "job",
  id: 101,
  exitCode: ExitStatus.COMPLETED,
  status: JobStatus.COMPLETED,
  serviceRequestType: JobType.STAGING,
  startTime: 10102203101,
  endTime: 20102203101,
  createUser: "admin",
  requestMessage: "",
  errors: [],
  yarnApplicationTrackingUrl: "string.twine.com"
};

describe("Staging Selectors", () => {
  const key = "testing";

  describe("getStagingState", () => {
    it(`should select the ${NAMESPACE} state`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getStagingState(state)).toEqual({});
    });

    it(`should return undefined if the ${NAMESPACE} state does not exist`, () => {
      expect(StagingSelectors.getStagingState({})).toBeUndefined();
    });
  });

  describe("getStagingFactory", () => {
    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(StagingSelectors.getStagingFactory(key)(state)).toEqual(
        initialStagingState
      );
    });

    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getStagingFactory(key)(state)).toBeUndefined();
    });
  });

  describe("getStagingCollectionFactory ", () => {
    it(`should select the collection property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(StagingSelectors.getStagingCollectionFactory(key)(state)).toEqual(
        initialStagingState.collection
      );
    });

    it(`should return the initial collection state if there is no collection property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getStagingCollectionFactory(key)(state)).toEqual(
        initialStagingState.collection
      );
    });
  });

  describe("getStagingGetStateFactory", () => {
    it(`should select the get property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(StagingSelectors.getStagingGetStateFactory(key)(state)).toEqual(
        initialStagingState.get
      );
    });

    it(`should return the initial get state if there is no get property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getStagingGetStateFactory(key)(state)).toEqual(
        initialStagingState.get
      );
    });
  });

  describe("getStagingByIdFactory", () => {
    it(`should select the nothing in ${NAMESPACE} state as collection is empty`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(
        StagingSelectors.getStagingByIdFactory(100, key)(state)
      ).toBeNull();
    });

    it(`should return null if not Jobs found for id`, () => {
      const jobExeuction: JobExecution = {
        ...populatedJob,
        id: 101
      };

      const stagingState: StagingState = {
        ...initialStagingState,
        collection: [jobExeuction]
      };

      const state = {
        [NAMESPACE]: {
          [key]: stagingState
        }
      };
      expect(
        StagingSelectors.getStagingByIdFactory(100, key)(state)
      ).toBeNull();
    });

    it(`should return jobExecution if Job found for id in collection`, () => {
      const jobExecution: JobExecution = {
        ...populatedJob,
        id: 101
      };

      const stagingState: StagingState = {
        ...initialStagingState,
        collection: [jobExecution]
      };

      const state = {
        [NAMESPACE]: {
          [key]: stagingState
        }
      };
      expect(StagingSelectors.getStagingByIdFactory(101, key)(state)).toEqual(
        jobExecution
      );
    });

    it(`should return jobExecution if Job found for id in running jobs`, () => {
      const jobExecution: JobExecution = {
        ...populatedJob,
        id: 101
      };

      const stagingState: StagingState = {
        ...initialStagingState,
        runningJobs: [jobExecution]
      };

      const state = {
        [NAMESPACE]: {
          [key]: stagingState
        }
      };
      expect(StagingSelectors.getStagingByIdFactory(101, key)(state)).toEqual(
        jobExecution
      );
    });
  });

  describe("getStagingJobStateFactory", () => {
    it(`should select the post property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(StagingSelectors.getStagingJobStateFactory(key)(state)).toEqual(
        initialStagingState.staging
      );
    });

    it(`should return the initial staging state if there is no staging property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getStagingJobStateFactory(key)(state)).toEqual(
        initialStagingState.staging
      );
    });
  });

  describe("getStagingValidateStateFactory", () => {
    it(`should select the validation property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(
        StagingSelectors.getStagingValidateStateFactory(key)(state)
      ).toEqual(initialStagingState.validate);
    });

    it(`should return the initial validation state if there is no staging property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingValidateStateFactory(key)(state)
      ).toEqual(initialStagingState.validate);
    });
  });

  describe("getStagingGetLoadingStateFactory ", () => {
    it(`should select the "get.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(
        StagingSelectors.getStagingGetLoadingStateFactory(key)(state)
      ).toEqual(initialStagingState.get.loading);
    });

    it(`should return the "get.loading" initial state if there is no get property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingGetLoadingStateFactory(key)(state)
      ).toEqual(initialStagingState.get.loading);
    });
  });

  describe("getStagingGetDatasetsLoadingStateFactory  ", () => {
    it(`should select the "getDetails.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(
        StagingSelectors.getStagingGetDatasetsLoadingStateFactory(key)(state)
      ).toEqual(initialStagingState.getDetails.loading);
    });

    it(`should return the "getDetails.loading" initial state if there is no getDetails property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingGetDatasetsLoadingStateFactory(key)(state)
      ).toEqual(initialStagingState.getDetails.loading);
    });
  });

  describe("getStagingJobDetailsFactory", () => {
    it(`should select the details property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(StagingSelectors.getStagingJobDetailsFactory(key)(state)).toEqual(
        initialStagingState.details
      );
    });

    it(`should return the initial details state if there is no details property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getStagingJobDetailsFactory(key)(state)).toEqual(
        initialStagingState.details
      );
    });
  });

  describe("getDatasetJobHistoryFactory", () => {
    it(`should select the history property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialStagingState
        }
      };

      expect(StagingSelectors.getDatasetJobHistoryFactory(key)(state)).toEqual(
        initialStagingState.datasetHistory
      );
    });

    it(`should return the initial history state if there is no details property in the
    ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(StagingSelectors.getDatasetJobHistoryFactory(key)(state)).toEqual(
        initialStagingState.datasetHistory
      );
    });
  });

  describe("getStagingGetByIdLoadingFactory ", () => {
    it(`should select the "getById.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: { ...initialStagingState, getById: { loading: true, error: false } }
        }
      };

      expect(
        StagingSelectors.getStagingGetByIdLoadingFactory(key)(state)
      ).toEqual(true);
    });

    it(`should return the "getById.loading" initial state if there is no getById property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingGetByIdLoadingFactory(key)(state)
      ).toEqual(initialStagingState.get.loading);
    });
  });

  describe("getStagingGetByIdErrorFactory ", () => {
    it(`should select the "getById.error" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: { ...initialStagingState, getById: { loading: false, error: true } }
        }
      };

      expect(
        StagingSelectors.getStagingGetByIdErrorFactory(key)(state)
      ).toEqual(true);
    });

    it(`should return the "getById.error" initial state if there is no getById property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingGetByIdErrorFactory(key)(state)
      ).toEqual(initialStagingState.getById.error);
    });
  });

  describe("getStagingRunningJobsFactory ", () => {
    it(`should select the "runningJobs" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: { ...initialStagingState, runningJobs: [populatedJob] }
        }
      };

      expect(
        StagingSelectors.getStagingRunningJobsFactory(key)(state)
      ).toEqual([populatedJob]);
    });

    it(`should return the "runningJobs" initial state if there is no getRunningJobs property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingRunningJobsFactory(key)(state)
      ).toEqual(initialStagingState.runningJobs);
    });
  });

  describe("getStagingRunningJobsLoadingFactory ", () => {
    it(`should select the "getRunningJobs.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: { ...initialStagingState, getRunningJobs: { loading: true, error: false } }
        }
      };

      expect(
        StagingSelectors.getStagingRunningJobsLoadingFactory(key)(state)
      ).toEqual(true);
    });

    it(`should return the "getRunningJobs.loading" initial state if there is no getRunningJobs property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingRunningJobsLoadingFactory(key)(state)
      ).toEqual(initialStagingState.get.loading);
    });
  });

  describe("getStagingRunningJobsErrorFactory ", () => {
    it(`should select the "getRunningJobs.error" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: { ...initialStagingState, getRunningJobs: { loading: false, error: true } }
        }
      };

      expect(
        StagingSelectors.getStagingRunningJobsErrorFactory(key)(state)
      ).toEqual(true);
    });

    it(`should return the "getRunningJobs.error" initial state if there is no getRunningJobs property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        StagingSelectors.getStagingRunningJobsErrorFactory(key)(state)
      ).toEqual(initialStagingState.getRunningJobs.error);
    });
  });
});
