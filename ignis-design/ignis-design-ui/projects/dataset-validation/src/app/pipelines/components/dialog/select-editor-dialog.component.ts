import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { IResizeEvent } from 'angular2-draggable/lib/models/resize-event';
import { Subscription } from 'rxjs';
import { Schema } from '../../../schemas';
import { SelectResult } from '../../interfaces/pipeline-step.interface';
import { StepSelectEditorService } from '../../services/step-select-editor.service';

@Component({
  selector: 'dv-select-editor-dialog',
  templateUrl: './select-editor-dialog.component.html',
  styleUrls: ['./select-editor-dialog.component.scss']
})
export class SelectEditorDialogComponent implements OnInit, OnDestroy {
  selectControl;
  initialValue: string;
  schemasIn: Schema[];

  selectName: string;
  productId: number;
  fieldIndex: number;
  fieldId: number;
  unionSchemaIn: number;

  dialogHeight: number;
  syntaxChecked = false;

  subscriptions: Subscription[] = [];
  result: SelectResult;

  editorHeight;

  constructor(
    private dialogRef: MatDialogRef<SelectEditorDialogComponent>,
    private selectEventService: StepSelectEditorService,
    @Inject(MAT_DIALOG_DATA) public data
  ) {
    this.selectControl = data.selectControl;
    this.initialValue = data.selectControl.value;
    this.schemasIn = data.schemasIn;
    this.selectName = data.selectName;
    this.fieldIndex = data.fieldIndex;
    this.fieldId = data.fieldId;
    this.unionSchemaIn = data.unionSchemaIn;
    this.dialogHeight = document.documentElement.clientHeight * 0.8;
    this.setEditorHeight();
  }

  syntaxValid(): boolean {
    return !this.result || this.result.valid;
  }

  done(): void {
    this.selectEventService.selectValueChange.emit({
      fieldIndex: this.fieldIndex,
      unionSchemaIn: this.unionSchemaIn,
      value: this.selectControl.value
    });
    this.dialogRef.close();
  }

  close() {
    this.selectEventService.selectValueChange.emit({
      fieldIndex: this.fieldIndex,
      unionSchemaIn: this.unionSchemaIn,
      value: this.initialValue
    });
    this.dialogRef.close();
  }

  checkSyntax() {
    this.selectEventService.checkSyntax.emit({
      fieldId: this.fieldId,
      sparkSql: this.selectControl.value
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(x => x.unsubscribe());
  }

  ngOnInit(): void {
    const valueChangeSubscription = this.selectControl.valueChanges
      .subscribe(x => this.syntaxChecked = false);

    const checkSyntaxSubscription = this.selectEventService.checkSyntaxSuccess
      .subscribe(result => {
        this.result = result;
        this.syntaxChecked = true;
      });
    this.subscriptions.push(checkSyntaxSubscription);
    this.subscriptions.push(valueChangeSubscription);
  }

  onResize(event: IResizeEvent): void {

    const selectEditorDialogContainerHeight = document
      .getElementById('select-editor-dialog-container')
      .clientHeight;
    const resizeHeight = event.size.height;

    if (resizeHeight > selectEditorDialogContainerHeight) {
      this.dialogHeight = selectEditorDialogContainerHeight;
    } else {
      this.dialogHeight = resizeHeight;
    }
    this.setEditorHeight();
  }

  setEditorHeight(): void {
    this.editorHeight = this.dialogHeight - 206;
  }
}
