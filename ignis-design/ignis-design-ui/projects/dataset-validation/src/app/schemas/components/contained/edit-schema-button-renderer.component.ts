import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ProductContextInterface } from './product-context.interface';

@Component({
  selector: 'dv-edit-rule-btn',
  templateUrl: './edit-schema-button-renderer.component.html',
  styleUrls: ['./edit-schema-button-renderer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditSchemaButtonRendererComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;
  productId: number;
  productName: string;
  schemaId: number;
  cellValue: any;
  hover: boolean;

  constructor(public router: Router) {}

  onClick() {
    const url = this.router.createUrlTree(
      [`product-configs/${this.productId}/schemas/${this.schemaId}`],
      { queryParams: { productName: this.productName } }
    );
    window.open(url.toString(), '_blank');
  }

  agInit(params: ICellRendererParams & ProductContextInterface): void {
    this.params = params;
    this.schemaId = params.data.id;
    this.productId = params.productId;
    this.productName = params.productName;
    this.cellValue = params.value;
  }

  refresh(): boolean {
    return false;
  }
}
