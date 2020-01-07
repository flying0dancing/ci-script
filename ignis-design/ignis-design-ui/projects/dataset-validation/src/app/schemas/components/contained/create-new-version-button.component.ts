import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { Schema } from '../..';
import { max } from '../../../core/utilities';
import { SchemaEventService } from '../../services/schema-event.service';
import { ProductContextInterface } from './product-context.interface';

@Component({
  selector: 'dv-create-version-btn',
  templateUrl: './create-new-version-button.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CreateNewVersionButtonComponent
  implements ICellRendererAngularComp {
  params: ICellRendererParams;
  productId: number;
  schemaId: number;
  schemaDisplayName: string;
  schemaPhysicalName: string;

  private schemas: Schema[] = [];

  constructor(private schemaEventService: SchemaEventService) {}

  onClick() {
    const maxSchema = max(
      (this.schemas ? this.schemas : []).filter(
        schema => schema.physicalTableName === this.schemaPhysicalName
      ),
      schema => schema.majorVersion
    );

    this.schemaEventService.openNewSchemaVersionDialog.emit({
      schemaId: this.schemaId,
      schemaDisplayName: this.schemaDisplayName,
      previousStartDate: maxSchema ? maxSchema.startDate : null
    });
  }

  agInit(params: ICellRendererParams & ProductContextInterface): void {
    this.params = params;
    this.schemaId = params.data.id;
    this.schemaDisplayName = params.data.displayName;
    this.schemaPhysicalName = params.data.physicalTableName;

    this.productId = params.productId;
    this.schemas = params.schemas;
  }

  refresh(): boolean {
    return false;
  }
}
