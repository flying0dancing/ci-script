import { CoreModule } from '@/core';
import { JobType } from '@/core/api/staging/staging.interfaces';
import * as populated from '@/fcr/dashboard/_tests/test.fixtures';
import { JobRowMenuRendererComponent } from '@/fcr/dashboard/jobs/job-row-menu-renderer.component';
import { ImportProductJobContainerComponent } from '@/fcr/dashboard/jobs/product/import-product-job-container.component';
import { RollbackProductJobContainerComponent } from '@/fcr/dashboard/jobs/product/rollback-product-job-container.component';
import { StagingJobContainerComponent } from '@/fcr/dashboard/jobs/staging/staging-job-container.component';
import { ValidationJobContainerComponent } from '@/fcr/dashboard/jobs/validation/validation-job-container.component';
import * as DialogsConstants from '@/shared/dialogs/dialogs.constants';
import * as DialogHelpers from '@/test-helpers/dialog.helpers';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { RouterTestingModule } from '@angular/router/testing';

describe("JobRowMenuRendererComponent", () => {
  let component: JobRowMenuRendererComponent;
  let fixture: ComponentFixture<JobRowMenuRendererComponent>;
  let dialog: MatDialog;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        CoreModule,
        MatDialogModule,
        MatMenuModule,
        DialogHelpers.createDialogTestingModule({
          declarations: [
            ImportProductJobContainerComponent,
            RollbackProductJobContainerComponent,
            StagingJobContainerComponent,
            ValidationJobContainerComponent
          ],
          entryComponents: [
            ImportProductJobContainerComponent,
            RollbackProductJobContainerComponent,
            StagingJobContainerComponent,
            ValidationJobContainerComponent
          ],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      declarations: [JobRowMenuRendererComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    dialog = TestBed.get(MatDialog);
    fixture = TestBed.createComponent(JobRowMenuRendererComponent);
    component = fixture.componentInstance;
  }));

  describe("agInit", () => {
    it("should add item that can open staging job details dialog on click", () => {
      spyOn(dialog, "open").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          id: 3332,
          serviceRequestType: JobType.STAGING
        }
      });
      component.rowMenuItems.pop().onClick();

      expect(dialog.open).toHaveBeenCalledWith(StagingJobContainerComponent, {
        width: DialogsConstants.WIDTHS.MEDIUM,
        data: {
          jobExecutionId: 3332,
          datasetId: undefined
        }
      });
    });
    it("should add item that can open validation job details dialog on click", () => {
      spyOn(dialog, "open").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          id: 3332,
          serviceRequestType: JobType.VALIDATION,
          requestMessage: '123456'
        }
      });
      component.rowMenuItems.pop().onClick();

      expect(dialog.open).toHaveBeenCalledWith(
        ValidationJobContainerComponent,
        {
          width: DialogsConstants.WIDTHS.MEDIUM,
          data: {
            jobExecutionId: 3332,
            datasetId: 123456
          }
        }
      );
    });
    it("should add item that can open import product job details dialog on click", () => {
      spyOn(dialog, "open").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          id: 3332,
          serviceRequestType: JobType.IMPORT_PRODUCT
        }
      });
      component.rowMenuItems.pop().onClick();

      expect(dialog.open).toHaveBeenCalledWith(
        ImportProductJobContainerComponent,
        {
          width: DialogsConstants.WIDTHS.MEDIUM,
          data: {
            jobExecutionId: 3332,
            datasetId: undefined
          }
        }
      );
    });
    it("should add item that can open rollback product job details dialog on click", () => {
      spyOn(dialog, "open").and.callThrough();

      component.agInit({
        ...populated.cellRendererParams,
        data: {
          id: 3332,
          serviceRequestType: JobType.ROLLBACK_PRODUCT
        }
      });
      component.rowMenuItems.pop().onClick();

      expect(dialog.open).toHaveBeenCalledWith(
        RollbackProductJobContainerComponent,
        {
          width: DialogsConstants.WIDTHS.MEDIUM,
          data: {
            jobExecutionId: 3332,
            datasetId: undefined
          }
        }
      );
    });
  });
});
