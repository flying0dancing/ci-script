import { StagingActions, StagingInterfaces } from '@/core/api/staging/';
import { JobStatus, JobType } from '@/core/api/staging/staging.interfaces';
import { StagingService } from '@/core/api/staging/staging.service';
import { StagingJobId } from '@/core/api/staging/staging.types';
import { JobRowMenuRendererComponent } from '@/fcr/dashboard/jobs/job-row-menu-renderer.component';
import {
  openImportProductJobDetailsDialog,
  openPipelineJobDetailsDialog,
  openRollbackProductJobDetailsDialog,
  openStagingJobDetailsDialog,
  openValidationJobDetailsDialog
} from '@/fcr/dashboard/jobs/jobs.functions';
import { StopJobDialogComponent } from '@/fcr/dashboard/jobs/stop/stop-job-dialog.component';
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { GridUtilities } from '@/fcr/shared/grid/grid.utilities';
import * as DialogsConstants from '@/shared/dialogs/dialogs.constants';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { ColDef, GridApi, GridOptions } from 'ag-grid/main';
import { take } from 'rxjs/operators';
import { NAMESPACE } from '../jobs/jobs.constants';
import { fadeInFactory } from '../shared/animations';
import { ColumnDefinitions } from '../shared/grid';
import { StatusRendererComponent } from './renderers/status.renderer';

@Component({
  selector: "app-jobs-list",
  templateUrl: "./jobs-list.component.html",
  styleUrls: ["./jobs-list.component.scss"],
  animations: [fadeInFactory(NAMESPACE)],
  providers: [ LocaleTimePipe ]
})
export class JobsListComponent {
  @Input() runningJobs: StagingInterfaces.JobExecution[];
  // tslint:disable-next-line:no-output-on-prefix
  @Output() onDetailsClick: EventEmitter<any> = new EventEmitter();

  columnDefs: ColDef[] = [
    {
      field: "status",
      filter: "agTextColumnFilter",
      headerName: "Status",
      headerTooltip: "Status",
      width: 120,
      cellRendererFramework: StatusRendererComponent,
      filterParams: GridUtilities.serverSideFilterParams
    },
    {
      field: "id",
      tooltipField: "id",
      filter: "agNumberColumnFilter",
      headerName: "Job ID",
      headerTooltip: "Job ID",
      width: 130,
      filterParams: GridUtilities.serverSideFilterParams
    },
    {
      field: "name",
      tooltipField: "name",
      filter: "agTextColumnFilter",
      headerName: "Job name",
      headerTooltip: "Job name",
      width: 450,
      filterParams: GridUtilities.serverSideFilterParams
    },
    {
      field: "serviceRequestType",
      tooltipField: "serviceRequestType",
      filter: "agTextColumnFilter",
      headerName: "Job type",
      headerTooltip: "Job type",
      width: 130,
      filterParams: GridUtilities.serverSideFilterParams
    },
    {
      field: "endTime",
      tooltipField: "endTime",
      suppressFilter: true,
      headerName: "Job ended",
      headerTooltip: "Job ended",
      sort: "desc",
      width: 270,
      filterParams: GridUtilities.serverSideFilterParams,
      valueFormatter: params =>
        this.localeTime.transform(params.value)
    },
    {
      ...ColumnDefinitions.dotMenuDefinition,
      cellRendererFramework: JobRowMenuRendererComponent
    }
  ];
  customStyle = { height: "433px" };
  pageSize = 7;
  rowData = [];

  gridApi: GridApi;
  gridOptions: GridOptions = {
    enableColResize: true,
    enableFilter: true,
    enableSorting: true,
    overlayLoadingTemplate: "Loading Data",
    overlayNoRowsTemplate: "No Data Found",
    rowModelType: "serverSide",
    rowSelection: "single",
    suppressRowClickSelection: false,
    pagination: true,
    paginationPageSize: this.pageSize,
    paginationAutoPageSize: false,
    cacheBlockSize: this.pageSize,
    maxBlocksInCache: 100,
    onGridReady: params => this.setupGrid(params.api),
    getContextMenuItems: params => [
      {
        name: "View Details",
        action: () => this.handleJobsButtonClick(params.node.data)
      }
    ]
  };

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private jobsService: StagingService,
    private localeTime: LocaleTimePipe) {}

  setupGrid(gridApi: GridApi) {
    this.gridApi = gridApi;
    gridApi.setServerSideDatasource({ getRows: this.getRows.bind(this) });
  }

  public resetGrid() {
    if (this.gridApi) {
      this.gridApi.purgeServerSideCache();
    }
  }

  getRows(params): void {
    const pageNumber = Math.floor(params.request.endRow / this.pageSize) - 1;
    const sortModel = params.request.sortModel;
    const filterModel = params.request.filterModel;
    const filterModelKeys = Object.keys(filterModel);

    let pageRequest;
    let search;

    if (sortModel.length > 0) {
      pageRequest = {
        page: pageNumber, size: this.pageSize,
        sort: { property: sortModel[0].colId, direction: sortModel[0].sort }
      };
    } else {
      pageRequest = { page: pageNumber, size: this.pageSize };
    }

    if (filterModelKeys.length > 0) {
      search = {
      ...filterModel[filterModelKeys[0]],
        columnName: filterModelKeys[0],
        expressionType: 'simple'
      };
    } else {
      search = {
        expressionType: 'combined',
        operator: 'AND',
        filters: [
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STARTING' },
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STARTED' },
          { expressionType: 'simple', columnName: 'status', type: 'notEqual', filter: 'STOPPING' }
        ]
      };
    }

    this.jobsService.getJobs(pageRequest, search)
      .subscribe(response => {
        this.store.dispatch(new StagingActions.GetSuccess({
          reducerMapKey: NAMESPACE,
          jobs: response.data
        }));

        params.successCallback(response.data, response.page.totalElements);
      });
  }

  handleStopJobButtonClick(jobExecutionId: StagingJobId, jobStatus: JobStatus) {
    let jobDetailsDialogRef = this.dialog.open(StopJobDialogComponent, {
      width: DialogsConstants.WIDTHS.SMALL,
      data: {
        jobId: jobExecutionId,
        jobStatus
      }
    });

    jobDetailsDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(result => {
        if (result) {
          this.store.dispatch(
            new StagingActions.StopJob({
              reducerMapKey: NAMESPACE,
              jobExecutionId: jobExecutionId
            })
          );
        }
        jobDetailsDialogRef = undefined;
      });
  }

  handleJobsButtonClick(jobExecution: StagingInterfaces.JobExecution) {
    if (jobExecution.serviceRequestType === JobType.VALIDATION) {
      openValidationJobDetailsDialog(
        this.dialog,
        jobExecution.id,
        parseInt(jobExecution.requestMessage, 10)
      );
    } else if (jobExecution.serviceRequestType === JobType.STAGING) {
      openStagingJobDetailsDialog(this.dialog, jobExecution.id);
    } else if (jobExecution.serviceRequestType === JobType.IMPORT_PRODUCT) {
      openImportProductJobDetailsDialog(
        this.dialog,
        jobExecution.id
      );
    } else if (jobExecution.serviceRequestType === JobType.ROLLBACK_PRODUCT) {
      openRollbackProductJobDetailsDialog(
        this.dialog,
        jobExecution.id
      );
    } else if (jobExecution.serviceRequestType === JobType.PIPELINE) {
      openPipelineJobDetailsDialog(this.dialog, jobExecution.id);
    }
  }

  isStopping(job: StagingInterfaces.JobExecution): boolean {
    return job.status === StagingInterfaces.JobStatus.STOPPING;
  }
}
