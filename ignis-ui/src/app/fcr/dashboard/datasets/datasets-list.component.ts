import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import { Feature } from '@/core/api/features/features.interfaces';
import { DatasetRowMenuRendererComponent } from '@/fcr/dashboard/datasets/dataset-row-menu-renderer.component';
import { openPipelineJobDetailsDialog, openStagingJobDetailsDialogForDataset } from '@/fcr/dashboard/jobs/jobs.functions';
import { ValidationStatusRendererComponent } from '@/fcr/dashboard/jobs/renderers/validation-status.renderer';
import { ColumnDefinitions } from '@/fcr/dashboard/shared/grid';
import { LocaleDatePipe } from '@/fcr/shared/datetime/locale-date-pipe.component';
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ColDef, GridOptions } from 'ag-grid';

@Component({
  selector: "app-datasets-list",
  templateUrl: "./datasets-list.component.html",
  styleUrls: ["./datasets-list.component.scss"],
  providers: [ LocaleDatePipe, LocaleTimePipe ]
})
export class DatasetsListComponent implements OnChanges {
  @Input() datasets: Dataset[];
  @Input() feature: Feature;
  @Input() loading: boolean;

  public columnDefs: ColDef[] = [
    {
      headerName: "Name",
      headerTooltip: "Name",
      field: "name",
      tooltipField: "name",
      filter: "agTextColumnFilter",
      width: 200,
      hide: true
    },
    {
      headerName: "Schema",
      headerTooltip: "Schema",
      field: "table",
      tooltipField: "table",
      filter: "agTextColumnFilter",
      width: 220
    },
    {
      headerName: "Records",
      headerTooltip: "Records",
      field: "recordsCount",
      tooltipField: "recordsCount",
      filter: "agTextColumnFilter",
      width: 120
    },
    {
      headerName: "Run Key",
      headerTooltip: "Run Key",
      field: "runKey",
      tooltipField: "runKey",
      filter: "agTextColumnFilter",
      width: 120
    },
    {
      headerName: "Entity Code",
      headerTooltip: "Entity Code",
      field: "entityCode",
      tooltipField: "entityCode",
      filter: "agTextColumnFilter",
      width: 145
    },
    {
      headerName: "Reference Date",
      headerTooltip: "Reference Date",
      field: "localReferenceDate",
      filter: "agTextColumnFilter",
      width: 180,
      valueFormatter: params =>
        this.localeDate.transform(params.value)
    },
    {
      headerName: "Status",
      headerTooltip: "Status",
      field: "validationStatus",
      filter: "agTextColumnFilter",
      suppressResize: true,
      width: 120,
      cellRendererFramework: ValidationStatusRendererComponent,
      comparator: statusComparator
    },
    {
      headerName: "Last Updated",
      headerTooltip: "Last Updated",
      field: "lastUpdated",
      filter: "agTextColumnFilter",
      sort: "desc",
      width: 200,
      valueFormatter: params =>
        this.localeTime.transform(params.value)
    },
    {
      ...ColumnDefinitions.dotMenuDefinition,
      cellRendererFramework: DatasetRowMenuRendererComponent
    }
  ];

  public customOptions = { resizeToFit: false };
  public customStyle = { height: "450px" };

  public gridOptions: GridOptions = {
    enableColResize: true,
    enableFilter: true,
    enableSorting: true,
    paginationAutoPageSize: true,
    pagination: true,
    getContextMenuItems: params => [
      {
        name: params.node.data.pipelineJobId
          ? "View Pipeline Job Details"
          : "View Staging Job Details",
        action: () => {
          if (params.node.data.pipelineJobId) {
            openPipelineJobDetailsDialog(
              this.dialog,
              params.node.data.pipelineJobId
            );
          } else {
            openStagingJobDetailsDialogForDataset(
              this.dialog,
              params.node.data.id
            );
          }
        }
      }
    ]
  };

  constructor(private dialog: MatDialog,
              private localeDate: LocaleDatePipe,
              private localeTime: LocaleTimePipe) {}

  ngOnChanges(changes: SimpleChanges): void {}
}

export function statusComparator(
  status1: string,
  status2: string,
  data1,
  data2
): number {
  if (data1.data.hasRules && !data2.data.hasRules) {
    return -1;
  }

  if (!data1.data.hasRules && data2.data.hasRules) {
    return 1;
  }

  return status1.toLowerCase().localeCompare(status2.toLowerCase());
}
