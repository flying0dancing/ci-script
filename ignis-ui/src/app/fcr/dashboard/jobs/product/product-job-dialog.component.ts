import { StagingInterfaces } from '@/core/api/staging';
import { JobExecution } from '@/core/api/staging/staging.interfaces';
import { Component, Input, OnChanges } from '@angular/core';

@Component({
  selector: "app-product-job-dialog",
  templateUrl: "./product-job-dialog.component.html",
  styleUrls: ["./product-job-dialog.component.scss"]
})
export class ProductJobDialogComponent implements OnChanges {
  @Input() jobExecution: JobExecution;
  @Input() titlePrefix: string;

  jobIsFinished: boolean;

  ngOnChanges(): void {
    this.jobIsFinished = !StagingInterfaces.isRunning(this.jobExecution);
  }
}
