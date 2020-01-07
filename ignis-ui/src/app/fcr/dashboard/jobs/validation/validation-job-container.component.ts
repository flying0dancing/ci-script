import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import { JobExecution } from '@/core/api/staging/staging.interfaces';
import { ChangeDetectionStrategy, Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import * as DatasetSelectors from '../../datasets/datasets.selectors';
import * as JobsSelectors from '../jobs.selectors';

@Component({
  templateUrl: "./validation-job-container.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ValidationJobContainerComponent implements OnInit {
  public dataset$: Observable<Dataset>;
  public jobExecution$: Observable<JobExecution>;

  constructor(
    public dialogRef: MatDialogRef<ValidationJobContainerComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private store: Store<any>
  ) {}

  ngOnInit(): void {
    this.dataset$ = this.store.select(
      DatasetSelectors.getDatasetById(this.data.datasetId)
    );
    this.jobExecution$ = this.store.select(
      JobsSelectors.getStagingJobDetailsById(this.data.jobExecutionId)
    );
  }
}
