import { Component } from '@angular/core';
import { MatDialog } from '@angular/material';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';

import { ProductConfigsRepository } from '../../services/product-configs.repository';
import { ValidateProductDialogComponent } from './validate-product-dialog.component';

@Component({
  templateUrl: './export-product-config-renderer.component.html'
})
export class ExportProductConfigRendererComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;

  downloadUrl: string;
  fileName: string;

  constructor(
    private apiService: ProductConfigsRepository,
    private dialog: MatDialog
  ) {}

  agInit(params: ICellRendererParams): void {
    this.params = params;
    this.downloadUrl = `${this.apiService.productConfigsUrl}/${this.params.data.id}/file`;
    this.fileName = this.params.data.name;
  }

  refresh(): boolean {
    return false;
  }

  validateProduct() {
    this.dialog.open(ValidateProductDialogComponent, {
      width: '650px',
      height: '550px',
      disableClose: true,
      data: {
        productId: this.params.data.id,
        productName: this.params.data.name
      }
    });
  }

  checkSchemaCurrentCount() {
    return this.params.data.schemas.length > 0;
  }
}
