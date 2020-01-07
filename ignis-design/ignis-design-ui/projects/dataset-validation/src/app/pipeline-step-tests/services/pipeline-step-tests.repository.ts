import { HttpClient, HttpEvent, HttpParams, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentService } from '../../../environments/environment.service';
import { Identifiable } from '../../core/utilities/interfaces/indentifiable.interface';
import { CreateInputDataRowRequest } from '../interfaces/create-input-data-row-request.interface';
import { CreatePipelineStepTestRequest } from '../interfaces/create-pipeline-step-test-request.interface';
import { CreateExpectedDataRowRequest } from '../interfaces/created-expected-data-row-request.interface';
import { InputData, OutputData, PipelineStepInputRows, PipelineStepTest, Run } from '../interfaces/pipeline-step-test.interface';
import { UpdatePipelineStepTestRequest } from '../interfaces/update-pipeline-step-test-request.interface';
import { UpdateRowCellDataRequest } from '../interfaces/update-row-cell-data-request.interface';

@Injectable({ providedIn: 'root' })
export class PipelineStepTestsRepository {
  readonly baseUrl = `${this.environmentService.env.api.root}/pipelineStepTests`;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {}

  getAll(pipelineId: number): Observable<PipelineStepTest[]> {
    let params = new HttpParams();

    params = params.append('pipelineId', `${pipelineId}`);

    return this.http.get<PipelineStepTest[]>(this.baseUrl, { params });
  }

  getOne(id: number): Observable<PipelineStepTest> {
    return this.http.get<PipelineStepTest>(`${this.baseUrl}/${id}`);
  }

  create(request: CreatePipelineStepTestRequest): Observable<PipelineStepTest> {
    return this.http.post<PipelineStepTest>(this.baseUrl, request);
  }

  update(
    id: number,
    request: UpdatePipelineStepTestRequest
  ): Observable<PipelineStepTest> {
    return this.http.patch<PipelineStepTest>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<Identifiable> {
    return this.http.delete<Identifiable>(`${this.baseUrl}/${id}`);
  }

  getTestInputRows(id: number): Observable<PipelineStepInputRows> {
    return this.http.get<PipelineStepInputRows>(`${this.baseUrl}/${id}/inputDataRows`)
  }

  getTestOutputRows(id: number): Observable<OutputData[]> {
    return this.http.get<OutputData[]>(`${this.baseUrl}/${id}/outputDataRows`)
  }

  createInputDataRow(
    id: number,
    request: CreateInputDataRowRequest
  ): Observable<InputData> {
    return this.http.post<InputData>(
      `${this.baseUrl}/${id}/inputDataRows`,
      request
    );
  }

  deleteInputDataRow(id: number, rowId: number): Observable<Identifiable> {
    return this.http.delete<Identifiable>(
      `${this.baseUrl}/${id}/inputDataRows/${rowId}`
    );
  }

  updateRowCellData(
    id: number,
    rowId: number,
    rowCellDataId: number,
    request: UpdateRowCellDataRequest
  ): Observable<InputData> {
    return this.http.patch<InputData>(
      `${this.baseUrl}/${id}/rows/${rowId}/rowCells/${rowCellDataId}`,
      request
    );
  }

  createExpectedDataRow(
    id: number,
    request: CreateExpectedDataRowRequest
  ): Observable<InputData> {
    return this.http.post<InputData>(
      `${this.baseUrl}/${id}/expectedDataRows`,
      request
    );
  }

  deleteExpectedDataRow(id: number, rowId: number): Observable<Identifiable> {
    return this.http.delete<Identifiable>(
      `${this.baseUrl}/${id}/expectedDataRows/${rowId}`
    );
  }

  run(id: number): Observable<Run> {
    return this.http.post<Run>(`${this.baseUrl}/${id}/run`, null);
  }

  uploadInputDataRow(stepId, schemaId, formData: FormData, importType): Observable<HttpEvent<number>> {
    const request = new HttpRequest(
      'POST',
      `${this.baseUrl}/${stepId}/schemas/${schemaId}/import?type=${importType}`,
      formData,
      {
        reportProgress: true
      }
    );
    return this.http.request<number>(request);
  }

  exportDataRowDownloadUrl(
    stepId: number,
    schemaId: number,
    exportType: string
  ): string {
    return `${this.baseUrl}/${stepId}/schemas/${schemaId}/export?type=${exportType}`;
  }
}
