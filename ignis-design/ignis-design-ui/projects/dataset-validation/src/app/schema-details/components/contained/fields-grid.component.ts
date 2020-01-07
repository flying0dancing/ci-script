import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import { ColDef, GridOptions, GridReadyEvent } from 'ag-grid';
import { DeleteButtonRendererComponent } from '../../../core/grid/components/delete-button-renderer.component';
import {
  createDefaultGridOptions,
  createDefaultTextFilterOptions
} from '../../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../../core/grid/interfaces/delete-params.interface';
import { Field } from '../../../schemas';
import { EditFieldButtonRendererComponent } from '../../../schemas/components/contained/edit-field-button-renderer.component';
import { Delete } from '../../actions/field-form.actions';
import { FieldOrderRendererComponent } from './field-order-renderer.component';
import { FieldOrderRendererParams } from './field-order-renderer.params';

@Component({
  selector: 'dv-fields-grid',
  templateUrl: './fields-grid.component.html',
  styleUrls: ['../schema-details-container.component.scss']
})
export class FieldsGridComponent implements OnInit, OnChanges {
  @Input() fields: Field[] = [];
  @Input() orderedFieldIds: number[] = [];
  @Input() loading: boolean;
  @Input() schemaId: number;
  @Input() productId: number;

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    paginationPageSize: 10,
    suppressRowClickSelection: true,
    suppressCellSelection: true,
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent)
  };

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Name',
      cellRendererFramework: EditFieldButtonRendererComponent,
      field: 'name',
      tooltipField: 'name',
      pinned: true,
      width: 400
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Type',
      field: 'type',
      width: 180
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Nullable',
      field: 'nullable',
      width: 180
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Format',
      field: 'format',
      width: 300
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Max Length',
      field: 'maxLength',
      width: 180
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Min Length',
      field: 'minLength',
      width: 180
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Precision',
      field: 'precision',
      width: 180
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Scale',
      field: 'scale',
      width: 180
    }
  ];

  private onGridReady(readyEvent: GridReadyEvent) {
    if (this.loading) {
      readyEvent.api.showLoadingOverlay();
    } else {
      if (this.fields.length === 0) {
        readyEvent.api.showNoRowsOverlay();
      }
    }
  }

  ngOnInit(): void {
    this.columnDefs.unshift(this.createOrderColumnDef());

    this.columnDefs.push({
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this field?',
        fieldNameForMessage: 'name',
        gridDeleteAction: new Delete(0, this.schemaId, this.productId)
      } as DeleteRendererParams,
      suppressMenu: true,
      suppressResize: true,
      width: 65
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes.orderedFieldIds.isFirstChange()) {
      this.columnDefs = [
        this.createOrderColumnDef(),
        ...this.columnDefs.slice(1)
      ];
    }
  }

  private createOrderColumnDef(): ColDef {
    return {
      ...createDefaultTextFilterOptions(),
      cellRendererFramework: FieldOrderRendererComponent,
      headerName: 'Order',
      width: 180,
      cellRendererParams: {
        orderedFieldIds: this.orderedFieldIds
      } as FieldOrderRendererParams
    };
  }
}
