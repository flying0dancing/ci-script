import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { ColDef, GridApi, GridOptions, GridReadyEvent, RowSelectedEvent } from 'ag-grid';

import { Schema } from '../..';
import { DateRendererComponent } from '../../../core/grid/components/date-renderer.component';
import { DeleteButtonRendererComponent } from '../../../core/grid/components/delete-button-renderer.component';
import {
  createDefaultDateFilterOptions,
  createDefaultGridOptions,
  createDefaultTextFilterOptions
} from '../../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../../core/grid/interfaces/delete-params.interface';
import { asLocalDate } from '../../../core/utilities';
import * as schemas from '../../index';
import { SchemaGridEventService } from '../../services/schema-grid.service';
import { CopySchemaButtonComponent } from './copy-schema-button.component';
import { CreateNewVersionButtonComponent } from './create-new-version-button.component';
import { EditSchemaButtonRendererComponent } from './edit-schema-button-renderer.component';
import { ProductContextInterface } from './product-context.interface';

@Component({
  selector: 'dv-schema-grid',
  templateUrl: './schema-grid.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SchemaGridComponent implements OnInit {
  @Input() productId: number;
  @Input() productName: string;
  @Input() schemas: Schema[] = [];
  @Input() loading: boolean;
  @Input() readonly: boolean;
  @Input() selectable: boolean;
  @Input() pageSize: number;

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    rowMultiSelectWithClick: true,
    suppressCellSelection: true,
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent),
    onRowSelected: (event: RowSelectedEvent) => this.onRowSelected(event),
    onGridSizeChanged: () => this.resizeToFit()
  };

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Display Name',
      field: 'displayName',
      tooltipField: 'displayName',
      width: 350
    }
  ];

  editColumns: ColDef[] = [];
  private api: GridApi;

  constructor(private schemaSelectService: SchemaGridEventService) {}

  ngOnInit() {
    this.editColumns = this.getEditColumns();
    if (!this.readonly) {
      this.columnDefs = this.editColumns;
    }

    if (this.selectable) {
      this.gridOptions = {
        ...this.gridOptions,
        rowSelection: 'multiple'
      };
      this.columnDefs[0].checkboxSelection = true;
    }
  }

  private getEditColumns(): ColDef[] {
    return [
      {
        ...createDefaultTextFilterOptions(),
        headerName: 'Display Name',
        field: 'displayName',
        sort: 'desc',
        sortedAt: 1,
        cellRendererFramework: EditSchemaButtonRendererComponent,
        width: 320,
        cellRendererParams: {
          productName: this.productName,
          productId: this.productId
        } as ProductContextInterface
      },
      {
        ...createDefaultTextFilterOptions(),
        headerName: 'Physical Name',
        field: 'physicalTableName',
        tooltipField: 'physicalTableName',
        width: 200
      },
      {
        ...createDefaultTextFilterOptions(),
        headerName: 'Version',
        field: 'majorVersion',
        tooltipField: 'majorVersion',
        sort: 'desc',
        sortedAt: 1
      },
      {
        ...createDefaultDateFilterOptions(),
        headerName: 'Start Date',
        field: 'startDate',
        tooltip: params => asLocalDate(params.data.startDate),
        cellRendererFramework: DateRendererComponent
      },
      {
        ...createDefaultDateFilterOptions(),
        headerName: 'End Date',
        field: 'endDate',
        tooltip: params => asLocalDate(params.data.startDate),
        cellRendererFramework: DateRendererComponent
      },
      {
        ...createDefaultDateFilterOptions(),
        headerName: 'Created Date',
        field: 'createdTime',
        tooltip: params => asLocalDate(params.data.startDate),
        cellRendererFramework: DateRendererComponent
      },
      {
        cellRendererFramework: CopySchemaButtonComponent,
        suppressMenu: true,
        suppressResize: true,
        suppressToolPanel: true,
        width: 60,
      },
      {
        cellRendererFramework: CreateNewVersionButtonComponent,
        suppressMenu: true,
        suppressResize: true,
        suppressToolPanel: true,
        width: 60,
        cellRendererParams: {
          productId: this.productId,
          schemas: this.schemas
        } as ProductContextInterface
      },
      {
        cellRendererFramework: DeleteButtonRendererComponent,
        cellRendererParams: {
          title: 'Are you sure you want to delete this schema?',
          fieldNameForMessage: 'name',
          gridDeleteAction: new schemas.Delete(this.productId, 0)
        } as DeleteRendererParams,
        suppressMenu: true,
        suppressResize: true,
        suppressToolPanel: true,
        width: 80
      }
    ];
  }

  private onGridReady(params: GridReadyEvent): void {
    this.api = params.api;
    params.api.sizeColumnsToFit();

    if (this.loading) {
      params.api.showLoadingOverlay();
    } else {
      if (this.schemas.length === 0) {
        params.api.showNoRowsOverlay();
      }
    }
  }

  private onRowSelected(event: RowSelectedEvent): void {
    const allSelectedIds: number[] = event.api
      .getSelectedNodes()
      .map(node => node.data.id);

    this.schemaSelectService.selectEvent.emit({ allSelectedIds });
  }

  private resizeToFit(): void {
    this.api.sizeColumnsToFit();
  }
}
