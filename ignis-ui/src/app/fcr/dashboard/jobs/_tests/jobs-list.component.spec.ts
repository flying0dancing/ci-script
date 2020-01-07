import { CoreModule } from '@/core';
import { Pageable } from '@/core/api/common/pageable.interface';
import { getJobsResponse, job, validationJob } from '@/core/api/staging/_tests/staging.mocks';
import * as StagingActions from '@/core/api/staging/staging.actions';
import { ExitStatus, JobExecution, JobStatus, JobType } from '@/core/api/staging/staging.interfaces';
import { StagingService } from '@/core/api/staging/staging.service';
import { TablesService } from '@/core/api/tables/tables.service';
import { reducers } from '@/fcr/dashboard/_tests/dashboard-reducers.mock';
import * as populated from '@/fcr/dashboard/_tests/test.fixtures';
import { JobRowMenuRendererComponent } from '@/fcr/dashboard/jobs/job-row-menu-renderer.component';
import * as JobConstants from '@/fcr/dashboard/jobs/jobs.constants';
import { StagingJobContainerComponent } from '@/fcr/dashboard/jobs/staging/staging-job-container.component';
import { StopJobDialogComponent } from '@/fcr/dashboard/jobs/stop/stop-job-dialog.component';
import { ValidationJobContainerComponent } from '@/fcr/dashboard/jobs/validation/validation-job-container.component';
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';
import { DialogsConstants } from '@/shared/dialogs';
import { DialogHelpers } from '@/test-helpers';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { Store, StoreModule } from '@ngrx/store';
import { MenuItemDef } from 'ag-grid';
import { of } from 'rxjs';

import { JobsListComponent } from '../jobs-list.component';

describe("JobsListComponent", () => {
  let component: JobsListComponent;
  let fixture: ComponentFixture<JobsListComponent>;
  let store: Store<any>;
  let dialog: MatDialog;
  let dialogSpy: any;
  let subscribeSpy: any;
  let jobsService: StagingService;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        CoreModule,
        MatDialogModule,
        DateTimeModule,
        StoreModule.forRoot(reducers),
        DialogHelpers.createDialogTestingModule({
          declarations: [
            StagingJobContainerComponent,
            ValidationJobContainerComponent,
            StopJobDialogComponent
          ],
          entryComponents: [
            StagingJobContainerComponent,
            ValidationJobContainerComponent,
            StopJobDialogComponent
          ],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      providers: [TablesService, StagingService],
      declarations: [JobsListComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    dialog = TestBed.get(MatDialog);
    store = TestBed.get(Store);
    fixture = TestBed.createComponent(JobsListComponent);
    component = fixture.componentInstance;
    component.runningJobs = [];
    jobsService = TestBed.get(StagingService);
    subscribeSpy = jasmine.createSpy("subscribeSpy");
    dialogSpy = jasmine.createSpy("dialogSpy");
    spyOn(store, "dispatch").and.callThrough();
    spyOn(jobsService, "getJobs").and.callFake(() => of(getJobsResponse));
    fixture.detectChanges();
  }));

  describe("handleJobsButtonClick", () => {
    beforeEach(() => {
      spyOn(dialog, "open").and.callThrough();
    });

    it("should open the staging job container dialog", () => {
      component.handleJobsButtonClick(job);

      expect(dialog.open).toHaveBeenCalledWith(StagingJobContainerComponent, {
        width: DialogsConstants.WIDTHS.MEDIUM,
        data: {
          jobExecutionId: job.id,
          datasetId: undefined
        }
      });
    });

    it("should open the validation job container dialog", () => {
      component.handleJobsButtonClick(validationJob);

      expect(dialog.open).toHaveBeenCalledWith(
        ValidationJobContainerComponent,
        {
          width: DialogsConstants.WIDTHS.MEDIUM,
          data: {
            jobExecutionId: validationJob.id,
            datasetId: 12345
          }
        }
      );
    });

    it("should remove the jobs-detail dialog reference after it is closed", () => {
      component.handleJobsButtonClick(job);

      fakeAsync(() => {
        dialog.closeAll();
        fixture.detectChanges();

        tick(500);

        expect((<any>component).jobDetailsDialogRef).toBeUndefined();
      });
    });
  });

  describe("handleStopJobButtonClick", () => {
    it("should open confirmation dialog", () => {
      spyOn(dialog, "open").and.callThrough();

      component.handleStopJobButtonClick(1, JobStatus.STARTED);

      expect(dialog.open).toHaveBeenCalledWith(StopJobDialogComponent, {
        width: DialogsConstants.WIDTHS.SMALL,
        data: {
          jobId: job.id,
          jobStatus: JobStatus.STARTED
        }
      });
    });
  });

  describe("constructor", () => {
    it("should set the column definitions", () => {
      expect(component.columnDefs.length).toBe(6);
    });

    it(`should have row menu`, () => {
      expect(
        component.columnDefs.find(
          colDef => colDef.cellRendererFramework === JobRowMenuRendererComponent
        )
      ).toBeTruthy();
    });
  });

  describe("ag-Grid context menu item", () => {
    it("should call handleJobsButtonClick", () => {
      spyOn(component, "handleJobsButtonClick").and.callThrough();
      const params = { ...populated.getContextMenuItemsParams };
      const jobExecution: JobExecution = { ...job };
      params.node.data = jobExecution;

      const viewDetailsItem = component.gridOptions
        .getContextMenuItems(params)
        .pop() as MenuItemDef;
      viewDetailsItem.action();
      expect(component.handleJobsButtonClick).toHaveBeenCalledWith(
        jobExecution
      );
    });
  });

  describe("isStopping", () => {
    it("should return true is job status is STOPPING", () => {
      const stoppingJob: JobExecution = {
        createUser: "",
        endTime: 0,
        id: 1,
        name: "",
        serviceRequestType: JobType.STAGING,
        startTime: 0,
        status: JobStatus.STOPPING,
        exitCode: ExitStatus.EXECUTING,
        errors: null,
        requestMessage: "",
        yarnApplicationTrackingUrl: ""
      };

      expect(component.isStopping(stoppingJob)).toBeTruthy();
    });

    it("should return false if job status is not STOPPING", () => {
      const stoppedJob: JobExecution = {
        createUser: "",
        exitCode: ExitStatus.EXECUTING,
        endTime: 0,
        id: 1,
        name: "",
        serviceRequestType: JobType.STAGING,
        startTime: 0,
        status: JobStatus.STOPPED,
        errors: null,
        requestMessage: "",
        yarnApplicationTrackingUrl: ""
      };

      expect(component.isStopping(stoppedJob)).toBeFalsy();
    });
  });

  describe("getRows", () => {
    it("should call jobService with paging and filter out running jobs", () => {
      const params = {
        request: {
          endRow: 14,
          sortModel: [],
          filterModel: {}
        },
        successCallback: function(x: any, y: any): void {
          //noop
        }
      };

      component.getRows(params);

      const page: Pageable = { page: 1, size: 7 };
      const filter = {
        expressionType: 'combined',
        operator: 'AND',
        filters: [
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STARTING' },
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STARTED' },
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STOPPING' }
        ]
      };
      expect(jobsService.getJobs).toHaveBeenCalledWith(page, filter);
    });

    it("should dispatch GetSuccess action on get jobs success", () => {
      let data;
      let totalElements;

      const params = {
        request: {
          endRow: 14,
          sortModel: [],
          filterModel: {}
        },
        successCallback: function(x: any, y: any): void {
          data = x;
          totalElements = y;
        }
      };

      component.getRows(params);

      const action = new StagingActions.GetSuccess({
        reducerMapKey: JobConstants.NAMESPACE,
        jobs: getJobsResponse.data
      });

      expect(store.dispatch).toHaveBeenCalledWith(action);
      expect(data).toEqual(getJobsResponse.data);
      expect(totalElements).toEqual(getJobsResponse.page.totalElements);
    });

    it("should call jobService with sorting", () => {
      const params = {
        request: {
          endRow: 14,
          sortModel: [{
            colId: 'mycolumn',
            sort: 'asc'
          }],
          filterModel: {}
        },
        successCallback: function(x: any, y: any): void {
          //noop
        }
      };

      component.getRows(params);

      const page: Pageable = { page: 1, size: 7, sort: { property: 'mycolumn', direction: 'asc' }};
      const filter = {
        expressionType: 'combined',
        operator: 'AND',
        filters: [
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STARTING' },
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STARTED' },
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STOPPING' }
        ]
      };
      expect(jobsService.getJobs).toHaveBeenCalledWith(page, filter);
    });

    it("should call jobService with filtering", () => {
      const params = {
        request: {
          endRow: 14,
          sortModel: [],
          filterModel: {
            mycolumn: {
              type: 'contains',
              filter: 'some text'
            }
          }
        },
        successCallback: function(x: any, y: any): void {
          //noop
        }
      };

      component.getRows(params);

      const page: Pageable = { page: 1, size: 7 };
      const filter = {
        expressionType: 'simple',
        columnName: 'mycolumn',
        type: 'contains',
        filter: 'some text'
      };
      expect(jobsService.getJobs).toHaveBeenCalledWith(page, filter);
    });
  });
});
