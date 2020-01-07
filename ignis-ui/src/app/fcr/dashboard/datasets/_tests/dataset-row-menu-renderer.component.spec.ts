import { CoreModule } from "@/core";
import { ValidationStatus } from "@/core/api/datasets/datasets.interfaces";
import * as StagingActions from "@/core/api/staging/staging.actions";
import * as populated from "@/fcr/dashboard/_tests/test.fixtures";
import { DatasetRowMenuRendererComponent } from "@/fcr/dashboard/datasets/dataset-row-menu-renderer.component";
import * as JobsConstants from "@/fcr/dashboard/jobs/jobs.constants";
import { StagingJobContainerComponent } from "@/fcr/dashboard/jobs/staging/staging-job-container.component";
import { ValidationJobContainerComponent } from "@/fcr/dashboard/jobs/validation/validation-job-container.component";
import * as DialogsConstants from "@/shared/dialogs/dialogs.constants";
import * as DialogHelpers from "@/test-helpers/dialog.helpers";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { MatDialog } from "@angular/material/dialog";
import { MatMenuModule } from "@angular/material/menu";
import { RouterTestingModule } from "@angular/router/testing";
import { Store } from "@ngrx/store";

describe("DatasetRowMenuRendererComponent", () => {
  let component: DatasetRowMenuRendererComponent;
  let fixture: ComponentFixture<DatasetRowMenuRendererComponent>;
  let store: Store<any>;
  let dialog: MatDialog;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        CoreModule,
        MatMenuModule,
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
      declarations: [DatasetRowMenuRendererComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    dialog = TestBed.get(MatDialog);
    store = TestBed.get(Store);
    fixture = TestBed.createComponent(DatasetRowMenuRendererComponent);
    component = fixture.componentInstance;
  }));

  describe("agInit", () => {
    it("should add item that can open staging job details dialog on click", () => {
      spyOn(dialog, "open").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          id: 3332
        }
      });
      const viewStagingJobMenuItem = component.rowMenuItems.find(
        rowMenuItem => rowMenuItem.label === "View Dataset Staging History"
      );
      viewStagingJobMenuItem.onClick();

      expect(viewStagingJobMenuItem.disabled).toBeFalsy();
      expect(dialog.open).toHaveBeenCalledWith(StagingJobContainerComponent, {
        width: DialogsConstants.WIDTHS.MEDIUM,
        data: {
          datasetId: 3332
        }
      });
    });

    it("should add item that can open validation job details dialog on click", () => {
      spyOn(dialog, "open").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          validationJobId: 5,
          id: "33"
        }
      });
      const viewValidationJobMenuItem = component.rowMenuItems.find(
        rowMenuItem => rowMenuItem.label === "View Validation Job Details"
      );
      viewValidationJobMenuItem.onClick();

      expect(dialog.open).toHaveBeenCalledWith(
        ValidationJobContainerComponent,
        {
          width: DialogsConstants.WIDTHS.MEDIUM,
          data: {
            jobExecutionId: 5,
            datasetId: "33"
          }
        }
      );
    });

    it("should add item that runs a validation job when dataset not validated", () => {
      spyOn(store, "dispatch").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          name: "set1",
          id: 567,
          validationStatus: ValidationStatus.NOT_VALIDATED
        }
      });
      const runValidationJobItem = component.rowMenuItems.find(
        item => item.label === "Run Validation Job"
      );
      runValidationJobItem.onClick();

      expect(store.dispatch).toHaveBeenCalledWith(
        new StagingActions.Validate({
          reducerMapKey: JobsConstants.NAMESPACE,
          body: { name: "set1", datasetId: 567 }
        })
      );
    });

    it("should add item that runs a validation job when dataset failed validation", () => {
      spyOn(store, "dispatch").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          name: "set1",
          id: 57,
          validationStatus: ValidationStatus.VALIDATION_FAILED
        }
      });
      const runValidationJobItem = component.rowMenuItems.find(
        item => item.label === "Re-run Validation Job"
      );
      runValidationJobItem.onClick();

      expect(store.dispatch).toHaveBeenCalledWith(
        new StagingActions.Validate({
          reducerMapKey: JobsConstants.NAMESPACE,
          body: { name: "set1", datasetId: 57 }
        })
      );
    });

    it("should add disabled item that runs a validation job when dataset validated", () => {
      component.agInit({
        ...populated.cellRendererParams,
        data: {
          validationStatus: ValidationStatus.VALIDATED
        }
      });
      const runValidationJobMenuItem = component.rowMenuItems.find(
        item => item.label === "Re-run Validation Job"
      );

      expect(runValidationJobMenuItem.disabled).toBeTruthy();
    });

    it("should add disabled item that runs a validation job when dataset validating", () => {
      component.agInit({
        ...populated.cellRendererParams,
        data: {
          validationStatus: ValidationStatus.VALIDATING
        }
      });
      const runValidationJobMenuItem = component.rowMenuItems.find(
        item => item.label === "Re-run Validation Job"
      );

      expect(runValidationJobMenuItem.disabled).toBeTruthy();
    });

    it("should add disabled view validation job details item when dataset has no rules", () => {
      component.agInit({
        ...populated.cellRendererParams,
        data: {
          validationStatus: ValidationStatus.VALIDATING,
          hasRules: false
        }
      });
      const viewValidationJobMenuItem = component.rowMenuItems.find(
        item => item.label === "View Validation Job Details"
      );

      expect(viewValidationJobMenuItem.disabled).toBeTruthy();
    });

    it("should add disabled view validation job details item when dataset status is not validated", () => {
      component.agInit({
        ...populated.cellRendererParams,
        data: {
          validationStatus: ValidationStatus.NOT_VALIDATED,
          hasRules: true
        }
      });
      const viewValidationJobMenuItem = component.rowMenuItems.find(
        item => item.label === "View Validation Job Details"
      );

      expect(viewValidationJobMenuItem.disabled).toBeTruthy();
    });
  });
});
