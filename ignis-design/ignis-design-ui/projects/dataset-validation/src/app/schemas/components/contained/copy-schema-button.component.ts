import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { SchemaEventService } from '../../services/schema-event.service';

@Component({
  selector: 'dv-copy-btn',
  templateUrl: './copy-schema-button.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CopySchemaButtonComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;
  productId: number;
  schemaId: number;


  constructor(private schemaEventService: SchemaEventService) {}

  onClick() {
    this.schemaEventService.openCopySchemaDialog.emit(this.schemaId);
  }

  agInit(params: ICellRendererParams): void {
    this.params = params;
    this.schemaId = params.data.id;

  }

  refresh(): boolean {
    return false;
  }
}
