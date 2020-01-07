import {
  ChangeDetectionStrategy,
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from "@angular/core";
import { GridApi } from "ag-grid/dist/lib/gridApi";
import { ColDef, GridOptions } from "ag-grid/main";

import { GridCustomOptions } from "./grid-custom-options.interface";
import { GRID_FADE_IN } from "./grid.animations";
import { DEFAULT_CONTEXT_MENU_ITEMS } from "./grid.constants";

@Component({
  selector: "app-grid",
  templateUrl: "./grid.component.html",
  styleUrls: ["./grid.component.scss"],
  animations: [GRID_FADE_IN],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GridComponent implements OnInit, OnChanges {
  @Input() columnDefs: ColDef[];

  @Input() autoGroupColumnDef: ColDef;

  @Input() customOptions: GridCustomOptions;

  @Input() customStyle: { [key: string]: any };

  @Input() gridOptions: GridOptions;

  @Input() pinnedTopRowData: any[];

  @Input() rowData: any[];

  @Input() loading: boolean;

  agGridOptions: GridOptions;
  gridApi: GridApi;

  constructor() {
    this.customOptions = {
      enableExport: false,
      fileName: "",
      sheetName: "",
      resizeToFit: false
    };

    this.agGridOptions = {
      floatingFilter: false,
      rowHeight: 40,
      headerHeight: 58,
      suppressClickEdit: true,
      suppressRowClickSelection: true,
      toolPanelSuppressPivotMode: true,
      toolPanelSuppressPivots: true,
      toolPanelSuppressRowGroups: true,
      toolPanelSuppressValues: true,
      toolPanelSuppressSideButtons: true,
      getContextMenuItems: params =>
        DEFAULT_CONTEXT_MENU_ITEMS(params, this.agGridOptions.api),
      onGridReady: params => this.onGridReady(params)
    };
  }

  onGridReady(params) {
    this.gridApi = params.api;

    if (this.customOptions.resizeToFit) {
      setTimeout(() => this.gridApi.sizeColumnsToFit());
    }
  }

  ngOnInit(): void {
    this.agGridOptions = {
      ...this.agGridOptions,
      ...this.gridOptions
    };
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.gridApi) {
      this.handleLoading();
    }
  }

  private handleLoading(): void {
    if (this.loading) {
      setTimeout(() => this.gridApi.showLoadingOverlay());
    } else {
      setTimeout(() => this.gridApi.hideOverlay());
    }
  }
}
