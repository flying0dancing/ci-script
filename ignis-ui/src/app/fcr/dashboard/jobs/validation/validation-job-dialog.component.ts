import { StagingInterfaces } from '@/core/api/staging';
import { Dataset, JobExecution } from '@/core/api/staging/staging.interfaces';
import { Component, Input, OnChanges } from '@angular/core';

@Component({
  selector: "app-validation-job-dialog",
  templateUrl: "./validation-job-dialog.component.html",
  styleUrls: ["./validation-job-dialog.component.scss"]
})
export class ValidationJobDialogComponent implements OnChanges {
  private jobIsFinished: boolean;
  @Input() jobExecution: JobExecution;
  @Input() dataset: Dataset;

  ngOnChanges(): void {
    this.jobIsFinished = !StagingInterfaces.isRunning(this.jobExecution);
  }
}
