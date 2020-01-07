import { Component, Inject } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import * as PipelineActions from '../actions/pipelines.actions';

@Component({
  selector: 'dv-add-pipeline-dialog',
  templateUrl: './create-pipeline-dialog.component.html'
})
export class CreatePipelineDialogComponent {
  pipelineForm = this.formBuilder.group({
    name: null
  });
  nameControl = this.pipelineForm.controls.name;

  productId: number;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<CreatePipelineDialogComponent>,
    private store: Store<any>,
    @Inject(MAT_DIALOG_DATA) public data
  ) {
    this.productId = data.productId;
  }

  createPipeline(): void {
    const name: string = this.nameControl.value;
    const productId: number = this.productId;

    this.store.dispatch(new PipelineActions.Create({ name, productId }));

    this.dialogRef.close();
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
