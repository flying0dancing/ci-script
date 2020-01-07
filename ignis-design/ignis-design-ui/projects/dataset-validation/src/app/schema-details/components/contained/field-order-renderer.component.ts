import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { FieldOrderRendererParams } from './field-order-renderer.params';

@Component({
  templateUrl: './field-order-renderer.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FieldOrderRendererComponent implements ICellRendererAngularComp {
  order: number;

  refresh(): boolean {
    return false;
  }

  agInit(params: ICellRendererParams & FieldOrderRendererParams): void {
    this.order = params.orderedFieldIds.indexOf(params.data.id);
  }
}
