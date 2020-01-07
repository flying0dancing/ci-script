import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/internal/operators';
import { EnvironmentService } from '../../../environments/environment.service';
import { quietHeaders } from '../../core/interceptors/http-error/http-error.interceptor';
import { Identifiable } from '../../core/utilities/interfaces/indentifiable.interface';
import { CreatePipelineRequest } from '../interfaces/create-pipeline-request.interface';
import { Pipeline, PipelineConnectedSets, PipelineDisplayError, PipelineDisplayReposonseType } from '../interfaces/pipeline.interface';
import { UpdatePipelineRequest } from '../interfaces/update-pipeline-request.interface';

@Injectable({ providedIn: 'root' })
export class PipelinesRepository {
  readonly pipelinesUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.pipelinesUrl = `${this.environmentService.env.api.root}/pipelines`;
  }

  getOne(id: number): Observable<Pipeline> {
    return this.http.get<Pipeline>(`${this.pipelinesUrl}/${id}`);
  }

  getAll(): Observable<Pipeline[]> {
    return this.http.get<Pipeline[]>(this.pipelinesUrl);
  }

  create(request: CreatePipelineRequest): Observable<number> {
    return this.http
      .post<Identifiable>(this.pipelinesUrl, request)
      .pipe(map(identifiable => identifiable.id));
  }

  update(id: number, request: UpdatePipelineRequest): Observable<Pipeline> {
    return this.http.patch<Pipeline>(`${this.pipelinesUrl}/${id}`, request);
  }

  getPipelineEdges(
    id: number
  ): Observable<PipelineConnectedSets | PipelineDisplayError> {
    const headers = quietHeaders();
    return this.http
      .get<PipelineConnectedSets>(`${this.pipelinesUrl}/${id}/edges`, {
        headers
      })
      .pipe(
        map(connectedSets => {
          const withType: PipelineConnectedSets = {
            ...connectedSets,
            type: PipelineDisplayReposonseType.SUCCESS
          };
          return withType;
        }),
        catchError(err => {
          if (err instanceof HttpErrorResponse && err.status === 400) {
            const { error } = err;
            const pipelineDisplayError: PipelineDisplayError = error;

            const withType: PipelineDisplayError = {
              ...pipelineDisplayError,
              type: PipelineDisplayReposonseType.ERROR
            };
            return of(withType);
          }
          throw err;
        })
      );
  }

  delete(id): Observable<number> {
    return this.http
      .delete<Identifiable>(`${this.pipelinesUrl}/${id}`)
      .pipe(map(response => response.id));
  }
}
