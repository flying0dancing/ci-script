import { Dataset, ValidationStatus } from '@/core/api/datasets/datasets.interfaces';
import { DatasetsListComponent, statusComparator } from '@/fcr/dashboard/datasets/datasets-list.component';
import { StagingJobContainerComponent } from '@/fcr/dashboard/jobs/staging/staging-job-container.component';
import { ValidationJobContainerComponent } from '@/fcr/dashboard/jobs/validation/validation-job-container.component';
import * as DialogHelpers from '@/test-helpers/dialog.helpers';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';

const populatedDataset: Dataset = {
  id: 123,
  table: "my table",
  pipelineInvocationId: 1,
  pipelineJobId: 1,
  pipelineStepInvocationId: 1,
  tableId: 1,
  name: "my name",
  datasetType: "dataset type",
  validationStatus: ValidationStatus.VALIDATED,
  validationJobId: 32,
  createdTime: "my time",
  lastUpdated: "my updated time",
  metadataKey: "the key",
  metadata: "the metadata",
  recordsCount: 12,
  rowKeySeed: 12345,
  predicate: "1 == 1",
  entityCode: "my entity code",
  referenceDate: "my reference date",
  localReferenceDate: "1991-1-1",
  hasRules: true,
  runKey: 1
};

describe("DatasetsListComponent", () => {
  let component: DatasetsListComponent;
  let fixture: ComponentFixture<DatasetsListComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        DialogHelpers.createDialogTestingModule({
          declarations: [
            StagingJobContainerComponent,
            ValidationJobContainerComponent
          ],
          entryComponents: [
            StagingJobContainerComponent,
            ValidationJobContainerComponent
          ],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      declarations: [DatasetsListComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(DatasetsListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  describe("statusComparator", () => {
    it("should sort by dataset 1 with rules and then by validation status", () => {
      const dataset1: Dataset = {
        ...populatedDataset,
        validationStatus: ValidationStatus.NOT_VALIDATED,
        hasRules: true
      };

      const dataset2: Dataset = {
        ...populatedDataset,
        validationStatus: ValidationStatus.NOT_VALIDATED,
        hasRules: false
      };

      const result = statusComparator(
        ValidationStatus.NOT_VALIDATED,
        ValidationStatus.NOT_VALIDATED,
        { data: dataset1 },
        { data: dataset2 }
      );

      expect(result).toEqual(-1);
    });

    it("should sort by dataset 2 with rules and then by validation status", () => {
      const dataset1: Dataset = {
        ...populatedDataset,
        validationStatus: ValidationStatus.NOT_VALIDATED,
        hasRules: false
      };

      const dataset2: Dataset = {
        ...populatedDataset,
        validationStatus: ValidationStatus.NOT_VALIDATED,
        hasRules: true
      };

      const result = statusComparator(
        ValidationStatus.NOT_VALIDATED,
        ValidationStatus.NOT_VALIDATED,
        { data: dataset1 },
        { data: dataset2 }
      );

      expect(result).toEqual(1);
    });

    it("should sort validation status in alphabetical order", () => {
      const dataset1: Dataset = {
        ...populatedDataset,
        validationStatus: ValidationStatus.NOT_VALIDATED,
        hasRules: true
      };

      const dataset2: Dataset = {
        ...populatedDataset,
        validationStatus: ValidationStatus.VALIDATED,
        hasRules: true
      };

      const result = statusComparator(
        ValidationStatus.NOT_VALIDATED,
        ValidationStatus.VALIDATED,
        { data: dataset1 },
        { data: dataset2 }
      );

      expect(result).toEqual(-1);
    });
  });
});
