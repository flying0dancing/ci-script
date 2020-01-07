import { StagingActions } from '@/core/api/staging';
import { jobRunning, stagingJob } from '@/core/api/staging/_tests/staging.mocks';
import { StagingService } from '@/core/api/staging/staging.service';
import { reducers } from '@/fcr/dashboard/_tests/dashboard-reducers.mock';
import { JobsListComponent } from '@/fcr/dashboard/jobs/jobs-list.component';
import { StageJobDialogComponent } from '@/fcr/dashboard/jobs/stage-job-dialog.component';
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';
import { DialogHelpers } from '@/test-helpers';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Store, StoreModule } from '@ngrx/store';
import { NAMESPACE } from '../../jobs/jobs.constants';

import { JobsComponent } from '../jobs.component';

describe("JobsComponent", () => {
  let component: JobsComponent;
  let fixture: ComponentFixture<JobsComponent>;
  let store: Store<any>;
  let dialog: MatDialog;
  let dialogSpy: any;
  let subscribeSpy: any;
  let jobsListComponent: JobsListComponent;
  const reducerMapKey = NAMESPACE;
  const mockStageJobPost = { reducerMapKey, body: stagingJob };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatDialogModule,
        StoreModule.forRoot(reducers),
        HttpClientTestingModule,
        DateTimeModule,
        DialogHelpers.createDialogTestingModule({
          declarations: [StageJobDialogComponent],
          entryComponents: [StageJobDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      declarations: [JobsComponent, JobsListComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [StagingService]
    }).compileComponents();

    dialog = TestBed.get(MatDialog);
    store = TestBed.get(Store);
    fixture = TestBed.createComponent(JobsComponent);
    component = fixture.componentInstance;
    jobsListComponent = component.jobsListComponent;
    subscribeSpy = jasmine.createSpy("subscribeSpy");
    dialogSpy = jasmine.createSpy("dialogSpy");
    spyOn(store, "dispatch").and.callThrough();
    spyOn(component.jobsListComponent, "resetGrid");
    fixture.detectChanges();
  }));

  describe("successfulStageJobs$", () => {
    it("should not emit if there has been no successful create job post", () => {
      (<any>component).successfulStageJobs$.subscribe(subscribeSpy);

      store.dispatch(new StagingActions.StartStagingJob(mockStageJobPost));
      store.dispatch(new StagingActions.StartStagingJobFail({ reducerMapKey }));

      expect(subscribeSpy).toHaveBeenCalledTimes(0);
    });

    it("should only emit if there has been a successful create user post", () => {
      (<any>component).successfulStageJobs$.subscribe(subscribeSpy);

      store.dispatch(new StagingActions.StartStagingJob(mockStageJobPost));
      store.dispatch(
        new StagingActions.StartStagingJobSuccess({ reducerMapKey })
      );

      expect(subscribeSpy).toHaveBeenCalledTimes(1);
    });
  });

  describe("handleRefreshJobsButtonClick", () => {
    it("should dispatch the get running jobs action", () => {
      const action = new StagingActions.GetRunningJobs({
        reducerMapKey: reducerMapKey
      });

      component.handleRefreshJobsButtonClick();

      expect(store.dispatch).toHaveBeenCalledWith(action);
    });

    it("should reset jobs grid", () => {
      component.handleRefreshJobsButtonClick();

      expect(jobsListComponent.resetGrid).toHaveBeenCalled();
    });
  });

  describe("handleAddJobButtonClick", () => {
    beforeEach(() => {
      spyOn(dialog, "open").and.callThrough();
      component.handleAddJobButtonClick();
    });

    it("should open the stage jobs dialog", () => {
      expect(dialog.open).toHaveBeenCalledWith(
        StageJobDialogComponent,
        (<any>component).stageJobsDialogConfig
      );
    });

    it("should remove the stage job dialog reference after it is closed", fakeAsync(() => {
      dialog.closeAll();
      fixture.detectChanges();

      tick(500);

      expect((<any>component).stageJobDialogRef).toBeUndefined();
    }));
  });

  describe("refreshRunningJobs", () => {
    it("should reset jobs grid if job has finished", fakeAsync(() => {
      const previousRunningJobs = [jobRunning];
      const currentRunningJobs = [];

      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      fixture.detectChanges();
      tick(10000);

      expect(jobsListComponent.resetGrid).toHaveBeenCalled();
    }));

    it("should not reset jobs grid if job has not finished yet", fakeAsync(() => {
      const previousRunningJobs = [jobRunning];
      const currentRunningJobs = [jobRunning];

      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      fixture.detectChanges();
      tick(10000);

      expect(jobsListComponent.resetGrid).not.toHaveBeenCalled();
    }));

    it("should loop through the Get jobs action when running jobs are present", fakeAsync(() => {
      const previousRunningJobs = [jobRunning];
      const currentRunningJobs = [jobRunning];

      const action = new StagingActions.GetRunningJobs({ reducerMapKey });
      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      fixture.detectChanges();
      tick(10000);

      expect(store.dispatch).toHaveBeenCalledWith(action);
    }));

    it("should dispatch get jobs event once when method called multiple times", fakeAsync(() => {
      expect(store.dispatch).toHaveBeenCalledTimes(1);

      const previousRunningJobs = [jobRunning];
      const currentRunningJobs = [jobRunning];

      const action = new StagingActions.GetRunningJobs({ reducerMapKey });

      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);
      (<any>component).refreshRunningJobs([previousRunningJobs, currentRunningJobs]);

      fixture.detectChanges();
      tick(100000);

      expect(store.dispatch).toHaveBeenCalledTimes(2);
      expect(store.dispatch).toHaveBeenNthCalledWith(2, action);
    }));
  });

  describe("handleCloseStageJobsDialog", () => {
    it("should close the stage jobs dialog", fakeAsync(() => {
      component.handleAddJobButtonClick();
      fixture.detectChanges();
      tick(500);
      spyOn((<any>component).stageJobDialogRef, "close").and.callThrough();

      (<any>component).handleCloseStagingJobsDialog();

      expect((<any>component).stageJobDialogRef.close).toHaveBeenCalledTimes(1);
    }));
  });

  describe("ngOnInit", () => {
    it("should dispatch the get running jobs actions", () => {
      const action = new StagingActions.GetRunningJobs({ reducerMapKey });

      component.ngOnInit();

      expect(store.dispatch).toHaveBeenCalledWith(action);
    });
  });
});
