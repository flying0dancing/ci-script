import { ColDef, GridOptions, GridApi } from 'ag-grid';

import { DateSortOrder } from '../utilities';
import * as DateUtils from '../utilities/date-utils';

export function createDefaultGridOptions(): GridOptions {
  return {
    enableColResize: true,
    enableFilter: true,
    enableSorting: true,
    gridAutoHeight: true,
    headerHeight: 48,
    pagination: true,
    paginationPageSize: 5,
    rowHeight: 48,
    suppressContextMenu: true,
    toolPanelSuppressPivotMode: true,
    toolPanelSuppressPivots: true,
    toolPanelSuppressRowGroups: true,
    toolPanelSuppressSideButtons: true,
    toolPanelSuppressValues: true,
    getMainMenuItems: params =>
      params.defaultItems.filter(item => item !== 'resetColumns')
  };
}

export function createDefaultTextFilterOptions(): ColDef {
  return {
    filter: 'agTextColumnFilter',
    filterParams: {
      filterOptions: ['contains', 'equals'],
      suppressAndOrCondition: true,
      clearButton: true,
      newRowsAction: 'keep'
    }
  };
}

export function createDefaultDateFilterOptions(): ColDef {
  return {
    filter: 'agDateColumnFilter',
    filterParams: {
      filterOptions: ['equals', 'greaterThan', 'lessThan'],
      suppressAndOrCondition: true,
      clearButton: true,
      newRowsAction: 'keep',
      comparator: (filterDate, cellValue) =>
        DateUtils.compareDatesByYearMonthDate(
          filterDate,
          new Date(cellValue),
          DateSortOrder.Desc
        )
    }
  };
}

export function createDefaultSetFilterOptions(): ColDef {
  return {
    filter: 'agSetColumnFilter',
    filterParams: {
      clearButton: true,
      newRowsAction: 'keep'
    }
  };
}

export function updateGridOverlays(
  gridApi: GridApi,
  rowData: any[],
  isRowDataLoading: boolean
) {
  if (!gridApi) return;

  if (isRowDataLoading) {
    gridApi.showLoadingOverlay();
  } else {
    const hasRowData = rowData && rowData.length ? true : false;

    if (!hasRowData) {
      gridApi.showNoRowsOverlay();
    }
  }
}
