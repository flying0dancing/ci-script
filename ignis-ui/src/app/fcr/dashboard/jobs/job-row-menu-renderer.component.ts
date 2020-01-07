import { JobExecution, JobType } from '@/core/api/staging/staging.interfaces';
import {
  openImportProductJobDetailsDialog,
  openPipelineJobDetailsDialog,
  openRollbackProductJobDetailsDialog,
  openStagingJobDetailsDialog,
  openValidationJobDetailsDialog
} from '@/fcr/dashboard/jobs/jobs.functions';
import { RowMenuRendererComponent } from '@/fcr/dashboard/shared/grid/column-definitions/row-menu-renderer.component';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ICellRendererParams } from 'ag-grid';

@Component({
  selector: "app-job-row-menu",
  templateUrl:
    "../shared/grid/column-definitions/vertical-menu-renderer.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class JobRowMenuRendererComponent extends RowMenuRendererComponent {
  constructor(private dialog: MatDialog) {
    super();
  }

  agInit(params: ICellRendererParams): void {
    const jobExecution: JobExecution = params.data;

    if (jobExecution !== undefined) {
      if (jobExecution.serviceRequestType === JobType.VALIDATION) {
        this.rowMenuItems.push({
          icon: "event_available",
          label: "View Validation Job Details",
          onClick: () =>
            openValidationJobDetailsDialog(
              this.dialog,
              jobExecution.id,
              parseInt(jobExecution.requestMessage, 10)
            )
        });
      } else if (jobExecution.serviceRequestType === JobType.STAGING) {
        this.rowMenuItems.push({
          icon: "event_note",
          label: "View Staging Job Details",
          onClick: () =>
            openStagingJobDetailsDialog(this.dialog, jobExecution.id)
        });
      } else if (jobExecution.serviceRequestType === JobType.IMPORT_PRODUCT) {
        this.rowMenuItems.push({
          icon: "event_note",
          label: "View Import Product Job",
          onClick: () =>
            openImportProductJobDetailsDialog(
              this.dialog,
              jobExecution.id
            )
        });
      } else if (jobExecution.serviceRequestType === JobType.ROLLBACK_PRODUCT) {
        this.rowMenuItems.push({
          icon: "event_note",
          label: "View Rollback Product Job",
          onClick: () =>
            openRollbackProductJobDetailsDialog(
              this.dialog,
              jobExecution.id
            )
        });
      } else if (jobExecution.serviceRequestType === JobType.PIPELINE) {
        this.rowMenuItems.push({
          icon: "event_note",
          label: "View Pipeline Job Details",
          onClick: () =>
            openPipelineJobDetailsDialog(this.dialog, jobExecution.id)
        });
      }
    }
  }
}
