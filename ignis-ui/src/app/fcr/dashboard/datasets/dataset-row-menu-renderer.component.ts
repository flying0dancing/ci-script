import { Dataset, ValidationStatus } from '@/core/api/datasets/datasets.interfaces';
import * as StagingActions from '@/core/api/staging/staging.actions';
import { StagingState } from '@/core/api/staging/staging.reducer';
import * as JobsConstants from '@/fcr/dashboard/jobs/jobs.constants';
import {
  openPipelineJobDetailsDialog,
  openStagingJobDetailsDialogForDataset,
  openValidationJobDetailsDialog
} from '@/fcr/dashboard/jobs/jobs.functions';
import { RowMenuRendererComponent } from '@/fcr/dashboard/shared/grid/column-definitions/row-menu-renderer.component';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { ICellRendererParams } from 'ag-grid';

const VALIDATION_DISABLED_STATUSES = [
  ValidationStatus.VALIDATING,
  ValidationStatus.VALIDATED
];

@Component({
  selector: "app-dataset-row-menu",
  templateUrl:
    "../shared/grid/column-definitions/vertical-menu-renderer.component.html",
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DatasetRowMenuRendererComponent extends RowMenuRendererComponent {
  constructor(
    private dialog: MatDialog,
    private store: Store<StagingState>,
    private router: Router
  ) {
    super();
  }

  agInit(params: ICellRendererParams): void {
    const dataset: Dataset = params.data;
    const runPrefix =
      dataset.validationStatus === ValidationStatus.NOT_VALIDATED
        ? "Run"
        : "Re-run";
    const runValidationLabel = `${runPrefix} Validation Job`;

    this.rowMenuItems.push({
      icon: "play_circle_outline",
      label: runValidationLabel,
      disabled:
        !dataset.hasRules ||
        VALIDATION_DISABLED_STATUSES.findIndex(
          status => status === dataset.validationStatus
        ) !== -1,
      onClick: () =>
        this.store.dispatch(
          new StagingActions.Validate({
            reducerMapKey: JobsConstants.NAMESPACE,
            body: { name: dataset.name, datasetId: dataset.id }
          })
        )
    });

    this.rowMenuItems.push({
      icon: "event_available",
      label: "View Validation Job Details",
      disabled:
        !dataset.hasRules ||
        dataset.validationStatus === ValidationStatus.NOT_VALIDATED,
      onClick: () => {
        this.store.dispatch(
          new StagingActions.GetById({
            reducerMapKey: JobsConstants.NAMESPACE,
            jobId: dataset.validationJobId
          }));

        openValidationJobDetailsDialog(
          this.dialog,
          dataset.validationJobId,
          dataset.id
        );
      }
    });

    if (dataset.pipelineJobId) {
      this.rowMenuItems.push({
        icon: "event_note",
        label: "View Pipeline Job Details",
        onClick: () => {
          this.store.dispatch(
            new StagingActions.GetById({
              reducerMapKey: JobsConstants.NAMESPACE,
              jobId: dataset.pipelineJobId
            }));

          openPipelineJobDetailsDialog(this.dialog, dataset.pipelineJobId);
        }
      });
      this.rowMenuItems.push({
        icon: "launch",
        label: "Open DrillBack",
        onClick: () =>
          this.router.navigate(["drillback"], {
            queryParams: {
              pipelineInvocationId: dataset.pipelineInvocationId,
              pipelineStepInvocationId: dataset.pipelineStepInvocationId
            },
            queryParamsHandling: "merge"
          })
      });
    } else {
      this.rowMenuItems.push({
        icon: "event_note",
        label: "View Dataset Staging History",
        onClick: () =>
          openStagingJobDetailsDialogForDataset(this.dialog, dataset.id)
      });
    }
  }
}
