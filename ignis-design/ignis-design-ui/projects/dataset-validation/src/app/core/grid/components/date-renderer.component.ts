import { Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';

@Component({
  selector: 'dv-date-renderer',
  templateUrl: './date-renderer.component.html'
})
export class DateRendererComponent implements ICellRendererAngularComp {
  params: ICellRendererParams;

  agInit(params: ICellRendererParams): void {
    this.params = params;
  }

  refresh(): boolean {
    return false;
  }
}
