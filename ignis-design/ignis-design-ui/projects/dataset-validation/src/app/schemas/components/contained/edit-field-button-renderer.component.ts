import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output
} from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { FieldEventService } from '../../../schema-details/service/field-event.service';

@Component({
  selector: 'dv-edit-field-btn',
  templateUrl: './edit-field-rule-button-renderer.component.html',
  styleUrls: ['./edit-schema-button-renderer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditFieldButtonRendererComponent
  implements ICellRendererAngularComp {
  @Output() editEvent = new EventEmitter<number>();

  id: number;
  cellValue: any;
  hover: boolean;

  constructor(private fieldEventService: FieldEventService) {}

  onClick(event) {
    event.preventDefault();
    this.fieldEventService.editEvent.emit({
      id: this.id
    });
  }

  agInit(params: ICellRendererParams): void {
    this.id = params.data.id;
    this.cellValue = params.getValue();
  }

  refresh(): boolean {
    return false;
  }
}
