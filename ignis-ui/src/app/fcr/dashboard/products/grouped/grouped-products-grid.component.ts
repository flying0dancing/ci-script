import { Feature } from '@/core/api/features/features.interfaces';
import { Product } from '@/core/api/products/products.interfaces';
import { LocaleDatePipe } from '@/fcr/shared/datetime/locale-date-pipe.component';
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { Component, Input, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Store } from '@ngrx/store';
import { ColDef, GridApi, GridOptions, GridReadyEvent } from 'ag-grid';
import { ProductRowItem } from '../product-schema.interface';
import { ImportStatusRendererComponent } from '../renderers/import-status.renderer';
import { ViewRuleButtonRendererComponent } from '../view-rule-button-renderer.component';

@Component({
  selector: 'app-grouped-products-grid',
  templateUrl: './grouped-products-grid.component.html',
  styleUrls: ['./grouped-products-grid.component.scss'],
  providers: [ LocaleDatePipe, LocaleTimePipe ]
})
export class GroupedProductsGridComponent implements OnInit {
  @Input() products: ProductRowItem[];
  @Input() loading: boolean;

  quickFilterForm = this.formBuilder.group({
    quickFilter: null
  });

  customStyle = { height: '600px' };
  gridOptions: GridOptions = {
    enableColResize: true,
    enableFilter: true,
    enableSorting: true,
    pagination: true,
    paginationPageSize: 12,
    groupMultiAutoColumn: true,
    autoGroupColumnDef: {
      cellRendererParams: {
        suppressCount: true
      }
    },
    groupHideOpenParents: true,
    showToolPanel: false,
    toolPanelSuppressSideButtons: true,
    onGridReady: params => this.onGridReady(params)
  };

  columnDefs: ColDef[] = [
    {
      headerName: 'Product',
      headerTooltip: 'Product',
      field: 'productName',
      tooltipField: 'productName',
      filter: 'agTextColumnFilter',
      rowGroup: true,
      hide: true,
      width: 260,
      cellRenderer: 'agGroupCellRenderer'
    },
    {
      headerName: 'Product Version',
      headerTooltip: 'Product Version',
      headerClass: 'header-wrap',
      field: 'productVersion',
      filter: 'agTextColumnFilter',
      rowGroup: true,
      hide: true,
      width: 160
    },
    {
      headerName: 'Name',
      headerTooltip: 'Name',
      field: 'itemName',
      filter: 'agTextColumnFilter',
      width: 220,
      cellRendererFramework: ViewRuleButtonRendererComponent
    },
    {
      headerName: '',
      headerTooltip: 'Import Status',
      field: 'importStatus',
      suppressResize: true,
      suppressMenu: true,
      suppressMovable: true,
      suppressToolPanel: true,
      width: 50,
      cellRendererFramework: ImportStatusRendererComponent
    },
    {
      headerName: 'Version',
      headerTooltip: 'Version',
      field: 'version',
      tooltipField: 'version',
      filter: 'agNumberColumnFilter',
      width: 140
    },
    {
      headerName: 'Start',
      headerTooltip: 'Start Date',
      field: 'startDate',
      filter: 'agDateColumnFilter',
      valueFormatter: params => this.localeDate.transform(params.value),
      getQuickFilterText: params => this.localeTime.transform(params.value),
      tooltip: params => this.localeTime.transform(params.value),
      width: 160
    },
    {
      headerName: 'End',
      headerTooltip: 'End Date',
      field: 'endDate',
      filter: 'agDateColumnFilter',
      valueFormatter: params => this.localeDate.transform(params.value),
      getQuickFilterText: params => this.localeTime.transform(params.value),
      tooltip: params => this.localeTime.transform(params.value),
      width: 160
    },
    {
      headerName: 'Created',
      headerTooltip: 'Created Time',
      field: 'createdTime',
      filter: 'agDateColumnFilter',
      sort: 'desc',
      hide: true,
      valueFormatter: params => this.localeTime.transform(params.value),
      tooltip: params => this.localeTime.transform(params.value),
      width: 210
    }
  ];
  private api: GridApi;

  constructor(
    private store: Store<Feature>,
    private formBuilder: FormBuilder,
    private localeDate: LocaleDatePipe,
    private localeTime: LocaleTimePipe
  ) {}

  ngOnInit(): void {}

  private onGridReady(params: GridReadyEvent) {
    this.api = params.api;
    this.api.sizeColumnsToFit();

    this.api.addEventListener('rowDataChanged', () => {
      this.api.forEachNode(node => {
        if (node.group) {
          node.setExpanded(true);
        }
      });
    });
    this.quickFilterForm.controls.quickFilter.valueChanges.subscribe(
      quickFilter => {
        setTimeout(() => {
          this.api.setQuickFilter(quickFilter);
        }, 200);
      }
    );
  }
}
