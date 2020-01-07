import { JobStatus } from "@/core/api/staging/staging.interfaces";
import { Component, Inject, OnInit } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: "app-stop-job-dialog",
  templateUrl: "./stop-job-dialog.component.html"
})
export class StopJobDialogComponent implements OnInit {
  jobId: number;
  isStopping = false;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: any,
    public dialogRef: MatDialogRef<StopJobDialogComponent>
  ) {}

  ngOnInit() {
    this.jobId = this.data["jobId"];
    this.isStopping = this.data["jobStatus"] === JobStatus.STOPPING;
  }

  close(): void {
    this.dialogRef.close(true);
  }
}
