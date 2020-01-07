import { Dataset } from '@/core/api/staging/staging.interfaces';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: "app-staging-job-dialog-dataset-jobs",
  templateUrl: "./staging-job-dialog-dataset-jobs.component.html"
})
export class StagingJobDialogDatasetJobsComponent {

  @Input() stagingDatasets: Dataset[];

  @Output() selectedDatasetEvent: EventEmitter<Dataset> = new EventEmitter();

  public handleClickEvent(dataset) {
    this.selectedDatasetEvent.emit(dataset);
  }
}
