import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router } from '@angular/router';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';

@Component({
  templateUrl: './edit-pipeline-button-renderer.component.html',
  styleUrls: ['./edit-pipeline-button-renderer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditPipelineButtonRendererComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;
  pipelineId: number;
  productId: number;
  productName: string;
  cellValue: any;
  hover: boolean;

  constructor(public router: Router) {}

  onClick() {
    this.router.navigate([
      `product-configs/${this.productId}/pipelines/${this.pipelineId}`
    ]);
  }

  agInit(params: ICellRendererParams): void {
    this.params = params;
    this.pipelineId = params.data.id;
    this.productId = params.data.productId;
    this.cellValue = params.value;
  }

  refresh(): boolean {
    return false;
  }
}
