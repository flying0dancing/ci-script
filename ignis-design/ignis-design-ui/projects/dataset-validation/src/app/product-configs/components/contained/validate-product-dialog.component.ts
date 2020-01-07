import { NestedTreeControl } from '@angular/cdk/tree';
import { Component, Inject, NgZone, OnDestroy, OnInit } from '@angular/core';
import { MatCheckboxChange } from '@angular/material';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { Store } from '@ngrx/store';
import { of, Subject, Subscription } from 'rxjs';
import { map, takeUntil } from 'rxjs/operators';
import { GetTaskList, Validate, ValidateCancel } from '../../actions/product-configs.actions';
import { ProductConfigTaskList, TaskStatus, ValidationTask } from '../../interfaces/product-validation.interface';
import { getProductValidationStatus, PRODUCTS_VALIDATED_STATE, PRODUCTS_VALIDATING_STATE } from '../../reducers/product-configs.selectors';
import { ProductConfigsRepository } from '../../services/product-configs.repository';
import { ConfirmDownloadDialogComponent } from './confirm-download-dialog.component';

@Component({
  selector: 'dv-validate-product-config',
  templateUrl: './validate-product-dialog.component.html',
  styleUrls: ['./validate-product-dialog.component.scss']
})
export class ValidateProductDialogComponent implements OnInit, OnDestroy {
  private readonly productId: number;
  downloadUrl: string;
  fileName: string;
  overallStatusIcon: string;

  private unsubscribe$ = new Subject<void>();

  validateButtonText = 'Validate';
  validating: boolean;
  validated: boolean;
  requiresNoValidation: boolean;

  selectAllPipelineCheckbox = true;
  pipelineCheckboxes = {};

  treeControl = new NestedTreeControl<ValidationTask>(validationTask =>
    of(validationTask.tasks)
  );
  dataSource = new MatTreeNestedDataSource<ValidationTask>();

  validatingSubscription: Subscription = this.store
    .select(PRODUCTS_VALIDATING_STATE)
    .pipe(takeUntil(this.unsubscribe$))
    .subscribe(validating =>
      this.ngZone.run(() => {
        this.validateButtonText = validating ? 'Validating' : this.validateButtonText;
        this.validating = validating;
      })
    );

  validatedSubscription: Subscription = this.store
    .select(PRODUCTS_VALIDATED_STATE)
    .pipe(takeUntil(this.unsubscribe$))
    .subscribe(validated =>
      this.ngZone.run(() => {
        this.validateButtonText = validated ? 'Validated' : this.validateButtonText;
        this.validated = validated;
      })
    );

  validationItemsSubscription: Subscription;

  constructor(
    private store: Store<any>,
    public dialogRef: MatDialogRef<ValidateProductDialogComponent>,
    private apiService: ProductConfigsRepository,
    private ngZone: NgZone,
    @Inject(MAT_DIALOG_DATA) public data,
    public dialog: MatDialog
  ) {
    this.productId = data.productId;
    this.downloadUrl = `${this.apiService.productConfigsUrl}/${this.productId}/file`;
    this.fileName = data.productName;
    this.overallStatusIcon = 'verified_user';

    this.validationItemsSubscription = this.store
      .select(getProductValidationStatus(this.productId))
      .pipe(
        takeUntil(this.unsubscribe$),
        map((tasks: ProductConfigTaskList) => {
          if (tasks) {
            return Object.values(tasks.pipelineTasks);
          }
        })
      )
      .subscribe((validationTasks: ValidationTask[]) =>
        this.ngZone.run(() => {
          this.dataSource.data = validationTasks;
          if (validationTasks) {
            validationTasks.forEach(validationTask => {
              this.pipelineCheckboxes[validationTask.pipelineId] = true;
            })
            this.enableDisableSelectAllPipelines();
            this.requiresNoValidation = validationTasks.length === 0;

            validationTasks.forEach(task => {
              if (task.status !== TaskStatus.SUCCESS) {
                this.treeControl.expand(task);
              }
            });
          }
        })
      );

    this.store.dispatch(new GetTaskList(this.productId));
  }

  ngOnInit(): void {}

  ngOnDestroy() {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

  validateProduct() {
    this.store.dispatch(new ValidateCancel(this.productId));
    this.store.dispatch(new Validate(this.productId));
  }

  cancel(): void {
    this.store.dispatch(new ValidateCancel(this.productId));
    this.dialogRef.close();
  }

  download() {
    const selectedPipelines = Object.keys(this.pipelineCheckboxes).filter(key => this.pipelineCheckboxes[key]);
    this.downloadUrl = this.downloadUrl.replace(/file.*/i, 'file/' + selectedPipelines.join());

    if (selectedPipelines.length === this.dataSource.data.length) {
      window.open(this.downloadUrl);
    } else {
      this.openConfirmationDialog();
    }
  }

  private openConfirmationDialog() {
    this.dialog.open(ConfirmDownloadDialogComponent, {
      width: '400px',
      data: {downloadUrl: this.downloadUrl}
    });
  }

  hasChild = (_: number, node: ValidationTask) =>
    !!node.tasks && node.tasks.length > 0;

  updatePipelineSelected($event: MatCheckboxChange, node) {
    if ($event.checked) {
      this.pipelineCheckboxes[node.pipelineId] = true;
    } else {
      this.pipelineCheckboxes[node.pipelineId] = false;
    }
    this.enableDisableSelectAllPipelines();
  }

  private enableDisableSelectAllPipelines() {
    if (this.allPipelinesSelected()) {
      this.selectAllPipelineCheckbox = true;
    }
    if (this.notAllPipelinesSelected()) {
      this.selectAllPipelineCheckbox = false;
    }
  }

  private allPipelinesSelected() {
    let allPipelinesSelected = true;
    for (const key in this.pipelineCheckboxes) {
      if (!this.pipelineCheckboxes[key]) {
        allPipelinesSelected = false;
      }
    }
    return allPipelinesSelected;
  }

  private notAllPipelinesSelected() {
    for (const key in this.pipelineCheckboxes) {
      if (!this.pipelineCheckboxes[key]) {
        return true;
      }
    }
    return false;
  }

  updateSelectAll($event: MatCheckboxChange) {
    if ($event.checked) {
      for (const key in this.pipelineCheckboxes) {
        this.pipelineCheckboxes[key] = true;
      }
    } else {
      for (const key in this.pipelineCheckboxes) {
        this.pipelineCheckboxes[key] = false;
      }
    }
  }
}
