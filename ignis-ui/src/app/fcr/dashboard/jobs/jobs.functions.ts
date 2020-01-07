import { PipelineJobDialogComponent } from '@/fcr/dashboard/jobs/pipeline/dialog/pipeline-job-dialog.component';
import { ImportProductJobContainerComponent } from '@/fcr/dashboard/jobs/product/import-product-job-container.component';
import { RollbackProductJobContainerComponent } from '@/fcr/dashboard/jobs/product/rollback-product-job-container.component';
import { StagingJobContainerComponent } from '@/fcr/dashboard/jobs/staging/staging-job-container.component';
import { ValidationJobContainerComponent } from '@/fcr/dashboard/jobs/validation/validation-job-container.component';
import * as DialogsConstants from '@/shared/dialogs/dialogs.constants';
import { ComponentType } from '@angular/cdk/portal';
import { MatDialog } from '@angular/material/dialog';
import { take } from 'rxjs/operators';

type JobDialogComponent =
  | ValidationJobContainerComponent
  | StagingJobContainerComponent
  | ImportProductJobContainerComponent
  | RollbackProductJobContainerComponent
  | PipelineJobDialogComponent;

export interface JobDetailsDialogConfig {
  jobExecutionId?: number;
  jobExecutionIds?: number[];
  datasetId?: number;
}

function openJobDetailsDialog<T>(
  dialog: MatDialog,
  dialogContainer: ComponentType<JobDialogComponent>,
  config: JobDetailsDialogConfig
): void {
  let jobDetailsDialogRef = dialog.open(dialogContainer, {
    width: DialogsConstants.WIDTHS.MEDIUM,
    data: config
  });

  jobDetailsDialogRef
    .afterClosed()
    .pipe(take(1))
    .subscribe(() => (jobDetailsDialogRef = undefined));
}

export function openStagingJobDetailsDialog(
  dialog: MatDialog,
  stagingJobId: number
): void {
  openJobDetailsDialog(dialog, StagingJobContainerComponent, {
    jobExecutionId: stagingJobId
  });
}

export function openStagingJobDetailsDialogForDataset(
  dialog: MatDialog,
  datasetId: number
): void {
  openJobDetailsDialog(dialog, StagingJobContainerComponent, {
    datasetId: datasetId
  });
}

export function openValidationJobDetailsDialog(
  dialog: MatDialog,
  validationJobId: number,
  datasetId: number
): void {
  openJobDetailsDialog(dialog, ValidationJobContainerComponent, {
      jobExecutionId: validationJobId,
      datasetId: datasetId
    }
  );
}

export function openImportProductJobDetailsDialog(
  dialog: MatDialog,
  importConfigJobId: number
): void {
  openJobDetailsDialog(
    dialog,
    ImportProductJobContainerComponent, {
      jobExecutionId: importConfigJobId
    }
  );
}

export function openRollbackProductJobDetailsDialog(
  dialog: MatDialog,
  importConfigJobId: number
): void {
  openJobDetailsDialog(dialog, RollbackProductJobContainerComponent, {
      jobExecutionId: importConfigJobId
    }
  );
}

export function openPipelineJobDetailsDialog(
  dialog: MatDialog,
  importConfigJobId: number
): void {
  openJobDetailsDialog(dialog, PipelineJobDialogComponent, {
    jobExecutionId: importConfigJobId
  });
}
