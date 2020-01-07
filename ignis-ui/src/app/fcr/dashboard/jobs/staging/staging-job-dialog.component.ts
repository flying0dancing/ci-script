import { StagingInterfaces } from '@/core/api/staging';
import { Dataset, JobExecution } from '@/core/api/staging/staging.interfaces';
import { Component, Input, OnChanges } from '@angular/core';

@Component({
  selector: "app-staging-job-dialog",
  templateUrl: "./staging-job-dialog.component.html",
  styleUrls: ["./staging-job-dialog.component.scss"]
})
export class StagingJobDialogComponent implements OnChanges {
  private jobIsFinished: boolean;
  @Input() jobExecution: JobExecution;
  @Input() stagingGetDetailsLoading: any;
  @Input() stagingGetJobDatasets: Dataset[];
  @Input() getByIdLoading: boolean;

  ngOnChanges(): void {
    this.jobIsFinished = !StagingInterfaces.isRunning(this.jobExecution);
  }
}
