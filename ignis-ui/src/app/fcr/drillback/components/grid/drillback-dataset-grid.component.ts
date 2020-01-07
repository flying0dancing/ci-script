import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import { PipelineStep, StepType } from '@/core/api/pipelines/pipelines.interfaces';
import { Field } from '@/core/api/tables/tables.interfaces';
import { gridApi } from '@/fcr/dashboard/_tests/test.fixtures';
import { getDatasetById } from '@/fcr/dashboard/datasets/datasets.selectors';
import { DrillbackHighlightService } from '@/fcr/drillback/drillback-highlight.service';
import { FCR_SYSTEM_COLUMNS } from '@/fcr/drillback/drillback-system.constants';
import { DatasetSchema, InputFieldToOutputField } from '@/fcr/drillback/drillback.interfaces';
import { DrillbackService } from '@/fcr/drillback/drillback.service';
import { GridUtilities } from '@/fcr/shared/grid/grid.utilities';
import { ChangeDetectionStrategy, Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { Store } from '@ngrx/store';
import { ColDef, ColumnResizedEvent, GridApi, GridOptions } from 'ag-grid';
import * as moment from 'moment';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-drillback-dataset-grid',
  templateUrl: './drillback-dataset-grid.component.html',
  styleUrls: ['./drillback-dataset-grid.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DrillbackDatasetGridComponent
  implements OnInit, OnDestroy, OnChanges {
  @Input()
  private isSourceDataset = false;
  @Input()
  private datasetId: number;
  @Input()
  private datasetSchema: DatasetSchema;
  @Input()
  private pipelineId: number;
  @Input()
  private pipelineStep: PipelineStep;
  @Input()
  private datasetRunOutdated: boolean;
  @Input()
  dataset: Dataset;
  @Input()
  drillbackMapping: InputFieldToOutputField;

  customStyle = { maxHeight: '100%', height: '685px' };
  pageSize = 14;

  gridOptions: GridOptions = {
    enableColResize: true,
    enableFilter: true,
    enableSorting: true,
    overlayLoadingTemplate: 'Loading Data',
    overlayNoRowsTemplate: 'No Data Found',
    rowModelType: 'serverSide',
    rowSelection: 'single',
    suppressRowClickSelection: false,
    pagination: true,
    paginationPageSize: this.pageSize,
    paginationAutoPageSize: false,
    cacheBlockSize: this.pageSize,
    maxBlocksInCache: 100,
    onGridReady: params => this.setupGrid(params.api),
    onRowSelected: event => this.onRowSelected(event),
    onColumnResized: columnResizeEvent => this.resizeRow(columnResizeEvent)
  };

  columnDefs: ColDef[] = [];
  rowData = [];
  selectedTargetRow: Map<string, Object>;
  showOutdatedDatasetWarning: boolean;
  selectedRowKey = 0;
  showDrillbackRows: boolean;
  sourceDataSet: boolean;

  private gridApi: GridApi;
  private drillBackDataSubscription: Subscription;
  private datasetSelectionSubscription: Subscription;
  private drillbackHighlightSubscription: Subscription;
  private drillbackSourceReloadSubscription: Subscription;

  private static convertToGridColumnDefinitions(
    datasetSchema: DatasetSchema
  ): ColDef[] {
    return datasetSchema.map(field =>
      DrillbackDatasetGridComponent.convertToColumnDefinition(field)
    );
  }

  private static convertToColumnDefinition(field: Field): ColDef {
    const colDef = {
      headerName: field.name,
      headerTooltip: field.name,
      field: field.name,
      filter: this.getFilterValue(field),
      cellRenderer: this.convertToDate(field.name, field.fieldType),
      filterParams: GridUtilities.serverSideFilterParams
    };
    if (field.fieldType === 'BOOLEAN') {
      return {
        ...colDef,
        filterParams: {
          ...colDef.filterParams,
          filterOptions: ['equals', 'notEqual'],
          defaultOption: 'equals'
        }
      };
    }
    return colDef;
  }

  private static getFilterValue(field: Field) {
    let filterValue = 'agTextColumnFilter';
    if (field.fieldType === 'STRING' || field.fieldType === 'BOOLEAN') {
      filterValue = 'agTextColumnFilter';
    } else if (field.fieldType === 'DATE' || field.fieldType === 'TIMESTAMP') {
      filterValue = 'agDateColumnFilter';
    } else {
      filterValue = 'agNumberColumnFilter';
    }
    return filterValue;
  }

  private static convertToDate(fieldName: string, fieldType: string) {
    if (fieldType === 'DATE') {
      return data => {
        return this.formatDate(data, 'DD/MM/YYYY');
      };
    }
    if (fieldType === 'TIMESTAMP') {
      return data => {
        return this.formatDate(data, 'DD/MM/YYYY HH:mm:ss');
      };
    }
  }

  private static formatDate(data, formatPattern) {
    if (data.getValue()) {
      return moment(data.value).format(formatPattern);
    } else {
      return null;
    }
  }

  constructor(
    private store: Store<any>,
    private drillBackHighlightService: DrillbackHighlightService,
    private drillBackService: DrillbackService
  ) {}

  ngOnInit(): void {
    this.sourceDataSet = this.isSourceDataset;
    if (this.datasetId && !this.dataset) {
      this.datasetSelectionSubscription = this.store
        .select(getDatasetById(this.datasetId))
        .subscribe(dataset => (this.dataset = dataset));
    }
    if (this.datasetSchema) {
      this.columnDefs = DrillbackDatasetGridComponent.convertToGridColumnDefinitions(
        this.datasetSchema
      );
    }

    if (this.isSourceDataset) {
      this.setupDrillbackSource();
    }

    this.showOutdatedDatasetWarning = this.datasetRunOutdated;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.gridApi) {
      this.setupGrid(this.gridApi);
    }
  }

  ngOnDestroy(): void {
    if (this.drillBackDataSubscription) {
      this.drillBackDataSubscription.unsubscribe();
    }
    if (this.datasetSelectionSubscription) {
      this.datasetSelectionSubscription.unsubscribe();
    }
    if (this.drillbackHighlightSubscription) {
      this.drillbackHighlightSubscription.unsubscribe();
    }
    if (this.drillbackSourceReloadSubscription) {
      this.drillbackSourceReloadSubscription.unsubscribe();
    }
  }

  private setupDrillbackSource() {
    this.drillbackHighlightSubscription = this.drillBackHighlightService.drillbackRowSelectEvent.subscribe(
      (row: Map<string, Object>) => {
        this.selectedTargetRow = row;
        if (gridApi && this.showDrillbackRows) {
          this.gridApi.setServerSideDatasource({
            getRows: this.getRows.bind(this)
          });
        }
        this.gridApi.redrawRows();
      }
    );

    this.gridOptions.suppressRowClickSelection = true;

    this.gridOptions.rowClassRules = {
      'drillback-row': this.isDrillbackRow.bind(this)
    };
  }

  private isDrillbackRow(params) {
    if (!params.data || !this.selectedTargetRow) {
      return false;
    }

    if (params.data[FCR_SYSTEM_COLUMNS.filteredByTransformation]) {
      return false;
    }

    if (
      this.pipelineStep.type === StepType.AGGREGATION &&
      (this.pipelineStep.groupings === null ||
        this.pipelineStep.groupings.length === 0)
    ) {
      return true;
    }

    const rowData: Map<string, Object> = params.data;
    const areDrillbackColumnsEqual = [];
    for (const rowDataKey in rowData) {
      if (this.drillbackMapping && this.drillbackMapping[rowDataKey] && this.selectedTargetRow) {
        const columnInOutput = this.drillbackMapping[rowDataKey];
        areDrillbackColumnsEqual.push(
          this.selectedTargetRow[columnInOutput] === rowData[rowDataKey]
        );
      }
    }

    return (
      this.selectedTargetRow && areDrillbackColumnsEqual.indexOf(false) === -1
    );
  }

  private setupGrid(setupGridApi: GridApi): void {
    this.gridApi = setupGridApi;
    this.gridApi.setServerSideDatasource({ getRows: this.getRows.bind(this) });
  }

  private getRows(getRowsParams): void {
    const rowsRequest = getRowsParams.request;

    const pageNumber: number =
      Math.floor(rowsRequest.endRow / this.pageSize) - 1;

    if (this.isSourceDataset) {
      const selectedRowKey = this.getShowDrillbackColumnDetails();
      this.drillBackService
        .getDrillbackInputRowData(
          this.dataset.id,
          this.pipelineId,
          this.pipelineStep.id,
          this.showDrillbackRows,
          selectedRowKey,
          this.pageSize,
          pageNumber,
          this.getSortArray(),
          this.getSearchParam(rowsRequest.filterModel)
        )
        .subscribe(({ data, page }) =>
          getRowsParams.successCallback(data, page.totalElements));
    } else {
      this.drillBackService
        .getDrillbackOutputRowData(
          this.dataset.id,
          this.pageSize,
          pageNumber,
          this.getSortArray(),
          this.getSearchParam(rowsRequest.filterModel)
        )
        .subscribe(({ data, page }) =>
          getRowsParams.successCallback(data, page.totalElements));
    }
  }

  private getShowDrillbackColumnDetails(): number {
    if (this.selectedTargetRow) {
      return this.selectedTargetRow['ROW_KEY'] as number;
    }
    return null;
  }

  private resizeRow(columnResizeEvent: ColumnResizedEvent): void {
    if (columnResizeEvent.finished) {
      this.gridApi.resetRowHeights();
    }
  }

  private getSortArray() {
    let sortArray = {};
    if (
      this.gridApi &&
      this.gridApi.getSortModel() &&
      this.gridApi.getSortModel().length > 0
    ) {
      sortArray = this.gridApi.getSortModel();
    }
    return sortArray;
  }

  private getSearchParam(filterModel): string {
    const filterParams = new Array();
    if (filterModel && Object.keys(filterModel).length > 0) {
      const filterKeys = Object.keys(filterModel);
      let filter = {};
      filterKeys.forEach(function(item, index) {
        const filterValue: Object = filterModel[item];
        filter = {
          ...filterValue,
          columnName: item,
          expressionType: 'simple'
        };
        filterParams.push(filter);
      });
    }
    let searchString = '';
    if (filterParams.length === 1) {
      searchString = encodeURI(JSON.stringify(filterParams[0]));
    }
    if (filterParams.length > 1) {
      searchString = encodeURI(
        JSON.stringify({
          expressionType: 'combined',
          operator: 'AND',
          filters: filterParams
        })
      );
    }
    return searchString;
  }

  private onRowSelected(event) {
    if (!this.isSourceDataset && event.node.selected) {
      this.drillBackHighlightService.drillbackRowSelectEvent.emit(event.data);
    }
  }

  ignoreWarning() {
    this.showOutdatedDatasetWarning = false;
  }

  onShowDrillbackRows($event) {
    if (this.selectedTargetRow) {
      if (gridApi) {
        this.gridApi.setServerSideDatasource({
          getRows: this.getRows.bind(this)
        });
      }
      this.gridApi.redrawRows();
    }
  }

  changeButtonHighlight(): 'dataset-button-selected' | 'dataset-button' {
    return this.showDrillbackRows ? 'dataset-button-selected' : 'dataset-button';
  }

  onClickShowOnlyDrillback($event: MouseEvent) {
    this.showDrillbackRows = !this.showDrillbackRows;
    this.onShowDrillbackRows(this.showDrillbackRows);
  }

  onClickExport($event: MouseEvent) {
    if (this.showDrillbackRows) {
      this.drillBackService.exportOnlyDrillbackDatasetAsCsv(this.datasetId,
        this.pipelineId, this.pipelineStep.id,
        this.selectedTargetRow['ROW_KEY'],
        this.getSortArray(),
        this.getSearchParam(this.gridApi.getFilterModel()));
    } else {
      this.drillBackService.exportDatasetAsCsv(this.datasetId, this.getSortArray(),
        this.getSearchParam(this.gridApi.getFilterModel()));
    }
  }
}
