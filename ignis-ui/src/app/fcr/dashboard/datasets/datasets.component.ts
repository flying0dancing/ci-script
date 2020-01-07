import { DatasetsActions } from "@/core/api/datasets/";
import { Dataset } from "@/core/api/datasets/datasets.interfaces";
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit
} from "@angular/core";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Store } from "@ngrx/store";
import { Observable, Subscription } from "rxjs";
import { filter, pairwise } from "rxjs/operators";
import { NAMESPACE } from "../datasets/datasets.constants";
import * as JobsSelectors from "../jobs/jobs.selectors";
import * as DatasetSelectors from "./datasets.selectors";

@Component({
  selector: "app-datasets",
  templateUrl: "./datasets.component.html",
  styleUrls: ["./datasets.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DatasetsComponent implements OnInit, OnDestroy {
  datasetsCollection$: Observable<Dataset[]> = this.store.select(
    DatasetSelectors.getDatasetCollection
  );
  datasetsGetLoading$: Observable<any> = this.store.select(
    DatasetSelectors.getDatasetLoading
  );

  private validationJobState$: Observable<any> = this.store.select(
    JobsSelectors.getStagingValidateState
  );
  private successfulValidationJob$ = this.validationJobState$.pipe(
    pairwise(),
    filter(
      ([prev, curr]) =>
        prev && curr && prev.loading && !curr.loading && !curr.error
    )
  );

  successfulValidationJobSubscription: Subscription;

  constructor(private store: Store<any>, public snackbar: MatSnackBar) {}

  handleRefreshDatasetsButtonClick() {
    this.store.dispatch(new DatasetsActions.Get({ reducerMapKey: NAMESPACE }));
  }

  ngOnInit() {
    this.store.dispatch(
      new DatasetsActions.Empty({ reducerMapKey: NAMESPACE })
    );
    this.store.dispatch(new DatasetsActions.Get({ reducerMapKey: NAMESPACE }));
    this.registerStartValidationJobSubscriptions();
  }

  private registerStartValidationJobSubscriptions() {
    this.successfulValidationJobSubscription = this.successfulValidationJob$.subscribe(
      () => this.handleSuccessfulStartedJob()
    );
  }

  private handleSuccessfulStartedJob() {
    this.snackbar.open("Validation Job Started", undefined, { duration: 3000 });
  }

  ngOnDestroy() {
    this.successfulValidationJobSubscription.unsubscribe();
  }
}
