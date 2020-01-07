import { Component, Input } from '@angular/core';
import { Store } from '@ngrx/store';
import { ColDef, GridOptions, GridReadyEvent, RowNode } from 'ag-grid';
import { Observable, Subscription } from 'rxjs';
import { DeleteButtonRendererComponent } from '../../../core/grid/components/delete-button-renderer.component';
import {
  createDefaultGridOptions,
  createDefaultTextFilterOptions
} from '../../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../../core/grid/interfaces/delete-params.interface';
import * as productConfigs from '../../actions/product-configs.actions';
import { ProductConfig } from '../../interfaces/product-config.interface';
import { PRODUCTS_CREATED_STATE } from '../../reducers/product-configs.selectors';
import { ExportProductConfigRendererComponent } from './export-product-config-renderer.component';
import { OpenProductConfigRendererComponent } from './open-product-config-renderer.component';

@Component({
  selector: 'dv-product-configs-grid',
  templateUrl: './product-configs-grid.component.html',
  styleUrls: ['./product-configs-grid.component.scss']
})
export class ProductConfigsGridComponent {
  @Input() productConfigs: ProductConfig[] = [];
  @Input() loading: boolean;

  created$: Observable<number[]> = this.store.select(PRODUCTS_CREATED_STATE);

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    suppressCellSelection: true,
    paginationPageSize: 12,
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent),
    onRowDataChanged: () => this.rowDataChanged()
  };

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Name',
      field: 'name',
      cellRendererFramework: OpenProductConfigRendererComponent
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Version',
      field: 'version'
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Schema Count',
      field: 'schemas.length',
      tooltip: params =>
        params.data.schemas.map(schema => schema.displayName).join(',  ')
    },
    {
      headerName: 'Actions',
      headerClass: 'actions-header',
      cellRendererFramework: ExportProductConfigRendererComponent,
      suppressResize: true,
      suppressMenu: true,
      cellStyle: { 'text-overflow': 'unset' },
      width: 40
    },
    {
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this product configuration?',
        fieldNameForMessage: 'name',
        gridDeleteAction: new productConfigs.Delete(0)
      } as DeleteRendererParams,
      suppressResize: true,
      suppressMenu: true,
      width: 40
    }
  ];

  createdProductSubscription: Subscription;

  constructor(private store: Store<any>) {}

  rowDataChanged(): void {
    this.createdProductSubscription = this.created$.subscribe(ids => {
      this.handleProductCreated(ids);

      if (this.createdProductSubscription) {
        this.createdProductSubscription.unsubscribe();
      }
    });
  }

  private handleProductCreated(ids: number[]) {
    if (this.gridOptions.api && ids) {
      const pageSize = this.gridOptions.api.paginationGetPageSize();

      this.gridOptions.api.forEachNodeAfterFilterAndSort((node: RowNode) => {
        const page = Math.floor(node.childIndex / pageSize);

        if (node.data.id === ids[0]) {
          this.gridOptions.api.paginationGoToPage(page);
          node.setSelected(true);
          setTimeout(() => node.setSelected(false), 2000);
        }
      });
    }
  }

  private onGridReady(readyEvent: GridReadyEvent) {
    readyEvent.api.sizeColumnsToFit();

    if (this.loading) {
      readyEvent.api.showLoadingOverlay();
    } else {
      if (this.productConfigs.length === 0) {
        readyEvent.api.showNoRowsOverlay();
      }
    }
  }
}
