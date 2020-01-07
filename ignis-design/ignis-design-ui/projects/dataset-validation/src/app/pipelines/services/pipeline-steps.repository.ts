import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EnvironmentService } from '../../../environments/environment.service';
import { quietHeaders } from '../../core/interceptors/http-error/http-error.interceptor';
import { ApiError, ErrorResponse } from '../../core/utilities/interfaces/errors.interface';
import { PipelineStep, SelectResult, SyntaxCheckRequest } from '../interfaces/pipeline-step.interface';

@Injectable({ providedIn: 'root' })
export class PipelineStepsRepository {
  private readonly baseUrl: string;
  private readonly pipelinesUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.baseUrl = `${this.environmentService.env.api.root}`;
    this.pipelinesUrl = `${this.environmentService.env.api.root}/pipelines`;
  }

  private stepsUrl(pipelineId: number) {
    return `${this.pipelinesUrl}/${pipelineId}/steps`;
  }

  save(pipelineId: number, step: PipelineStep) {
    const headers = quietHeaders();
    console.log('save', step);
    return this.http.post<PipelineStep>(`${this.stepsUrl(pipelineId)}`, step, { headers });
  }

  checkSyntax(request: SyntaxCheckRequest): Observable<SelectResult> {
    return this.http.post<SelectResult>(`${this.pipelinesUrl}/syntax`, request);
  }

  update(pipelineId: number, stepId: number, step: PipelineStep) {
    const headers = quietHeaders();
    return this.http.put(`${this.stepsUrl(pipelineId)}/${stepId}`, step, {
      headers
    });
  }

  delete(pipelineId: number, id: number): Observable<Response> {
    return this.http.delete<any>(`${this.stepsUrl(pipelineId)}/${id}`);
  }
}
