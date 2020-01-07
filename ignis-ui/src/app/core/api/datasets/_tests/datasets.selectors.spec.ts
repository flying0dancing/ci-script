import { Dataset, ValidationStatus } from '@/core/api/datasets/datasets.interfaces';
import { NAMESPACE } from '../datasets.constants';
import { DatasetsState, initialDatasetsState } from '../datasets.reducer';
import * as DatasetsSelectors from '../datasets.selectors';

export const populatedDataset: Dataset = {
  id: 1,
  table: "table",
  tableId: 1,
  pipelineInvocationId: 1,
  pipelineJobId: 1,
  pipelineStepInvocationId: 1,
  name: "name",
  datasetType: "datasetType",
  validationStatus: ValidationStatus.VALIDATED,
  validationJobId: 101,
  createdTime: "createdTime",
  lastUpdated: "lastUpdated",
  metadataKey: "metadataKey",
  metadata: "metadata",
  recordsCount: 100,
  rowKeySeed: 12345,
  predicate: "predicate",
  entityCode: "entityCode",
  referenceDate: "referenceDate",
  localReferenceDate: "1991-1-1",
  hasRules: true,
  runKey: 1
};

describe("Datasets Selectors", () => {
  const key = "testing";

  describe("getState", () => {
    it(`should select the ${NAMESPACE} state`, () => {
      const state = { [NAMESPACE]: {} };

      expect(DatasetsSelectors.getDatasetsState(state)).toEqual({});
    });

    it(`should return undefined if the ${NAMESPACE} state does not exist`, () => {
      expect(DatasetsSelectors.getDatasetsState({})).toBeUndefined();
    });
  });

  describe("getFactory", () => {
    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialDatasetsState
        }
      };

      expect(DatasetsSelectors.getDatasetsFactory(key)(state)).toEqual(
        initialDatasetsState
      );
    });

    it(`should select the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(DatasetsSelectors.getDatasetsFactory(key)(state)).toBeUndefined();
    });
  });

  describe("getCollectionFactory ", () => {
    it(`should select the collection of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialDatasetsState
        }
      };

      expect(
        DatasetsSelectors.getDatasetsCollectionFactory(key)(state)
      ).toEqual(initialDatasetsState.collection);
    });

    it(`should return the collection initial state if there is no collection in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        DatasetsSelectors.getDatasetsCollectionFactory(key)(state)
      ).toEqual(initialDatasetsState.collection);
    });
  });

  describe("getDatasetByIdFactory", () => {
    it(`should select nothing in ${NAMESPACE} state as collection is empty`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialDatasetsState
        }
      };

      expect(
        DatasetsSelectors.getDatasetByIdFactory(100, key)(state)
      ).toBeUndefined();
    });

    it(`should return null if no Datasets found for id`, () => {
      const dataset: Dataset = {
        ...populatedDataset,
        id: 101
      };

      const datasetState: DatasetsState = {
        ...initialDatasetsState,
        collection: [dataset]
      };

      const state = {
        [NAMESPACE]: {
          [key]: datasetState
        }
      };
      expect(
        DatasetsSelectors.getDatasetByIdFactory(100, key)(state)
      ).toBeUndefined();
    });

    it(`should return dataset if Dataset found for id`, () => {
      const dataset: Dataset = {
        ...populatedDataset,
        id: 101
      };

      const datasetState: DatasetsState = {
        ...initialDatasetsState,
        collection: [dataset]
      };

      const state = {
        [NAMESPACE]: {
          [key]: datasetState
        }
      };
      expect(DatasetsSelectors.getDatasetByIdFactory(101, key)(state)).toEqual(
        dataset
      );
    });
  });

  describe("getGetStateFactory ", () => {
    it(`should select the get property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialDatasetsState
        }
      };

      expect(DatasetsSelectors.getDatasetsGetStateFactory(key)(state)).toEqual(
        initialDatasetsState.get
      );
    });

    it(`should return the initial get state if there is no get property in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(DatasetsSelectors.getDatasetsGetStateFactory(key)(state)).toEqual(
        initialDatasetsState.get
      );
    });
  });

  describe("getDatasetsGetLoadingStateFactory", () => {
    it(`should select the "get.loading" property of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialDatasetsState
        }
      };

      expect(
        DatasetsSelectors.getDatasetsGetLoadingStateFactory(key)(state)
      ).toEqual(initialDatasetsState.get.loading);
    });

    it(`should return the "get.loading" initial state if there is no get property
      in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(
        DatasetsSelectors.getDatasetsGetLoadingStateFactory(key)(state)
      ).toEqual(initialDatasetsState.get.loading);
    });
  });

  describe("getSourceFilesFactory ", () => {
    it(`should select the sourceFiles of the ${NAMESPACE} state using the reducer key`, () => {
      const state = {
        [NAMESPACE]: {
          [key]: initialDatasetsState
        }
      };

      expect(DatasetsSelectors.getSourceFilesFactory(key)(state)).toEqual(
        initialDatasetsState.sourceFiles
      );
    });

    it(`should return the sourceFiles initial state if there is no sourceFiles in the ${NAMESPACE} state using the reducer key`, () => {
      const state = { [NAMESPACE]: {} };

      expect(DatasetsSelectors.getSourceFilesFactory(key)(state)).toEqual(
        initialDatasetsState.sourceFiles
      );
    });
  });
});
