import { StagingActions } from '@/core/api/staging';
import { job } from '@/core/api/staging/_tests/staging.mocks';
import { JobStatus } from '@/core/api/staging/staging.interfaces';
import { StagingJobContainerComponent } from '@/fcr/dashboard/jobs/staging/staging-job-container.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Store, StoreModule } from '@ngrx/store';
import { reducers } from '../../../_tests/dashboard-reducers.mock';

import { NAMESPACE } from '../../jobs.constants';

class MatDialogRefMock {}

describe("StagingJobContainerComponent", () => {
  let component: StagingJobContainerComponent;
  let fixture: ComponentFixture<StagingJobContainerComponent>;
  let store: Store<any>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.forRoot(reducers)],
      declarations: [StagingJobContainerComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { jobExecution: job } },
        { provide: MatDialogRef, useClass: MatDialogRefMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(StagingJobContainerComponent);
    component = fixture.componentInstance;
    store = TestBed.get(Store);
    spyOn(store, "dispatch").and.callThrough();
    fixture.detectChanges();
  });

  describe("getJobDetails", () => {
    it("should dispatch the GET_JOB_DATASETS action", () => {
      const action = new StagingActions.GetJobDatasets({
        reducerMapKey: NAMESPACE,
        jobExecutionId: component.data.jobExecutionId
      });
      component.getJobDetails(component.data.jobExecutionId);
      expect(store.dispatch).toHaveBeenCalledWith(action);
    });
  });

  describe("checkAndRegisterJobsSubscription", () => {
    it("should subscribe to the jobs collection if JobStatus equals STARTED", () => {
      (<any>component).checkAndRegisterJobsSubscription({
        status: JobStatus.STARTED
      });

      expect((<any>component).jobsCollectionSubscription).toBeDefined();
    });

    it("should not subscribe to the jobs collection if JobStatus is not STARTED", () => {
      (<any>component).checkAndRegisterJobsSubscription(JobStatus.COMPLETED);
      expect((<any>component).jobsCollectionSubscription).toBeUndefined();
    });
  });
});
