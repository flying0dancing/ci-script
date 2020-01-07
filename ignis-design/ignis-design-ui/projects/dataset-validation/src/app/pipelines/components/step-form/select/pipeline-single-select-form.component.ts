import { Component, EventEmitter, Input, NgZone, OnChanges, OnDestroy, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog } from '@angular/material';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Subscription } from 'rxjs';
import { Field, Schema } from '../../../../schemas';
import { Select, SelectResult } from '../../../interfaces/pipeline-step.interface';
import { SelectEditorChange, StepSelectEditorService } from '../../../services/step-select-editor.service';
import { SelectEditorDialogComponent } from '../../dialog/select-editor-dialog.component';

export const emptySelect: Select = {
  select: '',
  outputFieldId: null,
  order: 0,
  intermediateResult: false,
  window: {
    orderBy: [],
    partitionBy: []
  },
  hasWindow: false
};

export interface SelectForm extends Select {
  outputField: Field
}


@Component({
  selector: 'dv-single-select-form',
  templateUrl: './pipeline-single-select-form.component.html',
  styleUrls: ['./pipeline-single-select-form.component.scss'],
  providers: subformComponentProviders(PipelineSingleSelectFormComponent) // <-- Add this
})
export class PipelineSingleSelectFormComponent extends NgxSubFormRemapComponent<SelectForm, SelectForm> implements OnInit, OnDestroy, OnChanges {
  @Input()
  inputSchemas: Schema[];

  @Input()
  displayIndex: number;

  @Input()
  unionSchemaIn: number | null;

  @Input()
  selectResult: SelectResult = null;

  @Input()
  showOrder = true;

  @Output()
  changeOrder: EventEmitter<{ currentPosition: number, newPosition: number }> = new EventEmitter();

  private dialogChangesSubscription: Subscription;

  constructor(private dialog: MatDialog, private ngZone: NgZone, private selectEventService: StepSelectEditorService) {
    super();
  }

  editOrderFn: (value: number) => void = this.changeOrderOfSelect.bind(this);

  changeOrderOfSelect(value: string): void {
    const currentPosition = this.formGroupControls.order.value;
    this.changeOrder.emit({ currentPosition: currentPosition, newPosition: parseInt(value, 10) });
  };

  ngOnChanges(changes: SimpleChanges): void {
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

  expand(index: any, field: any) {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(SelectEditorDialogComponent, {
        disableClose: true,
        panelClass: 'select-editor-dialog-panel',
        data: {
          fieldIndex: index,
          fieldId: field.id,
          selectControl: this.formGroupControls.select,
          schemasIn: this.inputSchemas,
          selectName: field.name,
          unionSchemaIn: this.unionSchemaIn
        }
      });

      dialogRef.afterClosed().subscribe(() => {
        dialogRef = undefined;
      });
    });
  }

  protected transformToFormGroup(obj: SelectForm): SelectForm {
    return {
      ...obj,
      outputFieldId: obj.outputField ? obj.outputField.id : null,
    };
  }

  protected transformFromFormGroup(formValue: SelectForm): SelectForm {
    return {
      ...formValue,
      order: formValue.intermediateResult ? formValue.order : null
    };
  }

  protected getFormControls(): Controls<SelectForm> {
    return {
      select: new FormControl(),
      outputFieldId: new FormControl(),
      hasWindow: new FormControl(),
      intermediateResult: new FormControl(),
      window: new FormControl(),
      order: new FormControl(),
      outputField: new FormControl()
    };
  }

  private setupPopOutSelectDialogChanges() {
    this.dialogChangesSubscription = this.selectEventService.selectValueChange.subscribe(
      (event: SelectEditorChange) => {
        if (!!this.unionSchemaIn && event.unionSchemaIn !== this.unionSchemaIn) {
          return;
        }

        if (event.fieldIndex === this.displayIndex) {
          this.formGroupControls.select.setValue(event.value);
        }
      }
    );
  }
}
