import { DatasetsActions } from "@/core/api/datasets";
import * as StagingActions from "@/core/api/staging/staging.actions";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { StageJobDialogComponent } from "@/fcr/dashboard/jobs/stage-job-dialog.component";
import { DialogHelpers } from "@/test-helpers";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { Store, StoreModule } from "@ngrx/store";
import { Subscription } from "rxjs";
import { NAMESPACE } from "../../datasets/datasets.constants";
import { NAMESPACE as JOBS_NAMESPACE } from "../../jobs/jobs.constants";

import { DatasetsComponent } from "../datasets.component";

describe("DatasetsComponent", () => {
  let component: DatasetsComponent;
  let fixture: ComponentFixture<DatasetsComponent>;
  let store: Store<any>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        StoreModule.forRoot(reducers),
        MatSnackBarModule,
        DialogHelpers.createDialogTestingModule({
          declarations: [StageJobDialogComponent],
          entryComponents: [StageJobDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      declarations: [DatasetsComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    store = TestBed.get(Store);
    fixture = TestBed.createComponent(DatasetsComponent);
    spyOn(store, "dispatch").and.callThrough();

    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  describe("handleRefreshDatasetsButtonClick", () => {
    it("should dispatch the get datasets action", () => {
      const action = new DatasetsActions.Get({
        reducerMapKey: NAMESPACE
      });

      component.handleRefreshDatasetsButtonClick();
      expect(store.dispatch).toHaveBeenCalledWith(action);
    });
  });

  describe("ngOnInit", () => {
    it("should dispatch the empty and get datasets actions", () => {
      const emptyAction = new DatasetsActions.Empty({
        reducerMapKey: NAMESPACE
      });

      const getAction = new DatasetsActions.Get({
        reducerMapKey: NAMESPACE
      });

      component.ngOnInit();
      expect(store.dispatch).toHaveBeenCalledWith(emptyAction);
      expect(store.dispatch).toHaveBeenCalledWith(getAction);
    });

    it("should add subscription", () => {
      component.ngOnInit();

      expect(component.successfulValidationJobSubscription).toBeDefined();
    });

    it("should open snack bar when validation job started", () => {
      const body = { id: 100 };
      const snackBarSpy = spyOn(component.snackbar, "open");

      component.ngOnDestroy();
      component.ngOnInit();

      store.dispatch(
        new StagingActions.Validate({
          reducerMapKey: JOBS_NAMESPACE,
          body: { datasetId: 1, name: "test" }
        })
      );
      store.dispatch(
        new StagingActions.ValidateSuccess({
          reducerMapKey: JOBS_NAMESPACE,
          body: body,
          datasetId: 1
        })
      );

      expect(snackBarSpy).toHaveBeenCalledTimes(1);
      expect(snackBarSpy).toHaveBeenCalledWith(
        "Validation Job Started",
        undefined,
        { duration: 3000 }
      );
    });
  });

  describe("ngOnDestroy", () => {
    it("should remove subscription", () => {
      const subscription = new Subscription();
      const unsubscribeSpy = spyOn(subscription, "unsubscribe");

      component.successfulValidationJobSubscription = subscription;

      component.ngOnDestroy();

      expect(unsubscribeSpy).toHaveBeenCalledTimes(1);
    });
  });
});
