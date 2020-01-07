import { Component, EventEmitter, Input, NgZone, OnDestroy, OnInit, Output } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Subscription } from 'rxjs';
import { Field, Schema } from '../../../../schemas';
import { Order, OrderDirection, SelectResult } from '../../../interfaces/pipeline-step.interface';
import { SelectEditorChange, StepSelectEditorService } from '../../../services/step-select-editor.service';
import { SelectEditorDialogComponent } from '../../dialog/select-editor-dialog.component';
import { SelectForm } from '../select/pipeline-single-select-form.component';

export interface WindowSelectForm {
  select: string;
  outputFieldId: number;
  outputField: Field;
  intermediateResult: boolean;
  hasWindow: boolean;
  order: number;
  window: any;
  partitions: string[];
  orderBys: string[];
}

@Component({
  selector: 'dv-single-window-select-form',
  styleUrls: ['./pipeline-single-window-select-form.component.scss'],
  templateUrl: './pipeline-single-window-select-form.component.html',
  providers: subformComponentProviders(PipelineSingleWindowSelectFormComponent) // <-- Add this
})
export class PipelineSingleWindowSelectFormComponent
  extends NgxSubFormRemapComponent<SelectForm, WindowSelectForm>
  implements OnInit, OnDestroy {
  @Input()
  inputSchema: Schema;

  @Input()
  displayIndex: number;

  @Input()
  selectResult: SelectResult = null;

  @Output()
  changeOrder: EventEmitter<{ currentPosition: number, newPosition: number }> = new EventEmitter();

  editOrderFn: (value: number) => void = this.changeOrderOfSelect.bind(this);

  private dialogChangesSubscription: Subscription;

  constructor(
    private dialog: MatDialog,
    private ngZone: NgZone,
    private selectEventService: StepSelectEditorService
  ) {
    super();
  }

  ngOnInit(): void {
    this.setupPopOutSelectDialogChanges();
  }

  ngOnDestroy(): void {
    this.dialogChangesSubscription.unsubscribe();
  }

  newLineToArray(errorMessage: string): string[] {
    return errorMessage.split('\n');
  }

  private setupPopOutSelectDialogChanges() {
    this.dialogChangesSubscription = this.selectEventService.selectValueChange.subscribe(
      (event: SelectEditorChange) => {
        if (event.fieldIndex === this.displayIndex) {
          this.formGroupControls.select.setValue(event.value);
        }
      }
    );
  }

  protected transformToFormGroup(obj: SelectForm): WindowSelectForm {
    return {
      ...obj,
      outputFieldId: obj.outputField ? obj.outputField.id : null,
      partitions: obj.window ? obj.window.partitionBy : [],
      orderBys: obj.window
        ? obj.window.orderBy.map(
          order => `${order.fieldName} ${order.direction}`
        )
        : []
    };
  }

  protected transformFromFormGroup(formValue: WindowSelectForm): SelectForm {
    return {
      hasWindow: formValue.hasWindow,
      outputFieldId: formValue.outputFieldId,
      outputField: formValue.outputField,
      select: formValue.select,
      order: formValue.order,
      intermediateResult: formValue.intermediateResult,
      window: {
        partitionBy: formValue.partitions,
        orderBy: PipelineSingleWindowSelectFormComponent.buildOrders(
          formValue.orderBys
        )
      }
    };
  }

  protected getFormControls(): Controls<WindowSelectForm> {
    const selectFormControl = new FormControl();
    selectFormControl.setValidators(Validators.required);

    return {
      select: selectFormControl,
      outputFieldId: new FormControl(),
      outputField: new FormControl(),
      hasWindow: new FormControl(),
      intermediateResult: new FormControl(),
      order: new FormControl(),
      window: new FormControl(),
      partitions: new FormControl(),
      orderBys: new FormControl()
    };
  }

  expand(index: any, field: Field) {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(SelectEditorDialogComponent, {
        disableClose: true,
        panelClass: 'select-editor-dialog-panel',
        data: {
          fieldIndex: index,
          fieldId: field.id,
          selectControl: this.formGroupControls.select,
          schemasIn: [this.inputSchema],
          selectName: field.name,
        }
      });

      dialogRef.afterClosed().subscribe(() => {
        dialogRef = undefined;
      });
    });
  }


  changeOrderOfSelect(value: string): void {
    const currentPosition = this.formGroupControls.order.value;
    this.changeOrder.emit({ currentPosition: currentPosition, newPosition: parseInt(value, 10) });
  };


  private static buildOrders(orderByFields: string[]): Order[] {
    if (!orderByFields) {
      return [];
    }

    const orders: Order[] = [];

    for (let i = 0; i < orderByFields.length; i++) {
      const split = orderByFields[i].trim().split(' ');

      if (split.length > 1) {
        const fieldName = split[0];
        const direction = split[1];

        orders.push({
          fieldName: fieldName,
          direction:
            direction === OrderDirection.ASC
              ? OrderDirection.ASC
              : OrderDirection.DESC,
          priority: i
        });
      }
    }

    return orders;
  }
}
