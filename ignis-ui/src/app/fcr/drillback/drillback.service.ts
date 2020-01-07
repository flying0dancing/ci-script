import { DatasetRowData, DrillbackStepDetails } from '@/fcr/drillback/drillback.interfaces';
import { RequestParamBuilder } from '@/shared/utilities/request-param.utils';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

@Injectable()
export class DrillbackService {
  constructor(private http: HttpClient) {}

  static generateGetStepDrillBackSchema(): string {
    return `${environment.api.externalRoot}/drillback/pipelines`;
  }

  static generateGetDatasetRowData(): string {
    return `${environment.api.externalRoot}/drillback/datasets`;
  }

  getDrillbackColumnsForStep(
    pipelineId: number,
    stepId: number
  ): Observable<DrillbackStepDetails> {
    return this.http.get<DrillbackStepDetails>(
      `${DrillbackService.generateGetStepDrillBackSchema()}/${pipelineId}/steps/${stepId}`
    );
  }

  getDrillbackOutputRowData(
    datasetId: number,
    pageSize: number,
    pageNumber: number = 0,
    sortArray,
    searchParam: string
  ): Observable<DatasetRowData> {

    const paramBuilder = this.generateDrillbackParams(pageSize, pageNumber, searchParam, sortArray)
      .param('type', 'output');

    return this.http.get<DatasetRowData>(
      `${DrillbackService.generateGetDatasetRowData()}/${datasetId}${paramBuilder.toParamString()}`
    );
  }

  getDrillbackInputRowData(
    datasetId: number,
    pipelineId: number,
    pipelineStepId: number,
    showOnlyDrillbackRows: boolean,
    outputTableRowKey: number,
    pageSize: number,
    pageNumber: number = 0,
    sortArray,
    searchParam: string
  ): Observable<DatasetRowData> {

    let paramBuilder = this.generateDrillbackParams(pageSize, pageNumber, searchParam, sortArray)
      .param('type', 'input')
      .param('pipelineId', pipelineId)
      .param('pipelineStepId', pipelineStepId);

    if (showOnlyDrillbackRows && outputTableRowKey) {
      paramBuilder = paramBuilder.param('showOnlyDrillbackRows', showOnlyDrillbackRows)
        .param('outputTableRowKey', outputTableRowKey);
    }

    return this.http.get<DatasetRowData>(
      `${DrillbackService.generateGetDatasetRowData()}/${datasetId}${paramBuilder.toParamString()}`
    );
  }

  exportDatasetAsCsv(
    datasetId: number,
    sortArray,
    searchParam: string
  ) {

    let paramBuilder = new RequestParamBuilder();
    paramBuilder.param('search', searchParam);
    paramBuilder = this.addSortParameter(sortArray, paramBuilder);
    const url = `${DrillbackService.generateGetDatasetRowData()}/${datasetId}/export${paramBuilder.toParamString()}`;
    this.exportDatasetUsingUrl(url, datasetId);
  }

  private exportDatasetUsingUrl(url: string, datasetId: number) {
    this.http.get(url, {responseType: 'text'}).subscribe(
      response => this.downLoadFile(response, "application/csv", this.getDatasetFileName(datasetId))
    );
  }

  private getDatasetFileName(datasetId: number) {
    return  "drillback_dataset_".concat(datasetId.toString()).concat("_").concat(new Date().getTime().toString()).concat('.csv');
  }

  private downLoadFile(data: any, type: string, fileName: string) {
    const blob = new Blob([data], { type: type});
    const url = window.URL.createObjectURL(blob);
    const anchorElement = document.createElement('a');
    anchorElement.href = url;
    anchorElement.download = fileName;
    anchorElement.click();
    setTimeout( () => {
      URL.revokeObjectURL(url);
    }, 100);
  }

  private addSortParameter(sortArray, paramBuilder) {
    if (!!sortArray && sortArray.length > 0) {
      paramBuilder = paramBuilder.param(
        'sort',
        sortArray.map(sort => sort.colId + ',' + sort.sort)
          .join(','));
    }
    return paramBuilder;
  }

  exportOnlyDrillbackDatasetAsCsv(
    datasetId: number,
    pipelineId: number,
    pipelineStepId: number,
    outputTableRowKey: number,
    sortArray,
    searchParam: string
  ) {

    let paramBuilder = new RequestParamBuilder();
    paramBuilder.param('pipelineId', pipelineId);
    paramBuilder.param('pipelineStepId', pipelineStepId);
    paramBuilder.param('outputTableRowKey', outputTableRowKey);
    paramBuilder.param('search', searchParam);
    paramBuilder = this.addSortParameter(sortArray, paramBuilder);
    const url = `${DrillbackService.generateGetDatasetRowData()}/${datasetId}/exportOnlyDrillback${paramBuilder.toParamString()}`;
    this.exportDatasetUsingUrl(url, datasetId);
  }

  private generateDrillbackParams(pageSize: number,
                                  pageNumber: number,
                                  searchParam: string,
                                  sortArray): RequestParamBuilder {
    let paramBuilder = new RequestParamBuilder()
      .param('size', pageSize)
      .param('page', pageNumber)
      .param('search', searchParam);

    if (!!sortArray && sortArray.length > 0) {
      paramBuilder = paramBuilder.param(
        'sort',
        sortArray.map(sort => sort.colId + ',' + sort.sort)
          .join(','));
    }
    return paramBuilder;
  }

}
