import { JobExecution } from "@/core/api/staging/staging.interfaces";
import {
  ChangeDetectionStrategy,
  Component,
  Inject,
  OnInit
} from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";
import * as JobsSelectors from "../jobs.selectors";

@Component({
  templateUrl: "./import-product-job-container.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImportProductJobContainerComponent implements OnInit {
  public jobExecution$: Observable<JobExecution>;

  constructor(
    public dialogRef: MatDialogRef<ImportProductJobContainerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private store: Store<any>
  ) {}

  ngOnInit(): void {
    this.jobExecution$ = this.store.select(
      JobsSelectors.getStagingJobDetailsById(this.data.jobExecutionId)
    );
  }
}
