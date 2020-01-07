import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';

@Component({
  selector: 'dv-edit-product-btn',
  templateUrl: './open-product-config-renderer.component.html',
  styleUrls: ['./open-product-config-renderer.component.scss'],

  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OpenProductConfigRendererComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;
  cellValue: any;
  hover: boolean;

  agInit(params: ICellRendererParams): void {
    this.params = params;
    this.cellValue = params.getValue();
  }

  refresh(): boolean {
    return false;
  }
}
