import {
  Column,
  ColumnApi,
  ColumnResizedEvent,
  GetContextMenuItemsParams,
  GridApi,
  GridPanel,
  GridReadyEvent,
  ICellRendererParams,
  RowNode
} from "ag-grid";
import { noop } from "rxjs";

export const gridApi = new GridApi();
gridApi.registerGridComp(new GridPanel());

const empty = {};
const column = new Column(empty, "colId", false);
const rowNode = new RowNode();
const columnApi = new ColumnApi();

export const getContextMenuItemsParams: GetContextMenuItemsParams = {
  defaultItems: [],
  column: column,
  node: rowNode,
  value: "value",
  api: gridApi,
  columnApi: columnApi,
  context: empty
};

export const gridReadyEvent: GridReadyEvent = {
  type: "type",
  columnApi: columnApi,
  api: gridApi
};

export const cellRendererParams: ICellRendererParams = {
  value: empty,
  valueFormatted: empty,
  getValue: noop,
  setValue: noop,
  formatValue: noop,
  data: empty,
  node: rowNode,
  colDef: empty,
  column: column,
  $scope: empty,
  rowIndex: 0,
  api: gridApi,
  columnApi: columnApi,
  context: empty,
  refreshCell: noop,
  eGridCell: null,
  eParentOfValue: null,
  addRenderedRowListener: noop
};

export const columnResizedEvent: ColumnResizedEvent = {
  finished: false,
  column: column,
  columns: [column],
  source: "rowModelUpdated",
  columnApi: columnApi,
  api: gridApi,
  type: "type"
};
