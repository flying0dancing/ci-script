import {
  Pipeline,
  PipelineDownstream,
  PipelineEdge,
  PipelineInvocation,
  SchemaDetails
} from "@/core/api/pipelines/pipelines.interfaces";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

@Injectable()
export class PipelinesService {
  constructor(private http: HttpClient) {}

  static generateGetUrl(): string {
    return `${environment.api.externalRoot}/pipelines`;
  }

  static generateGetDownstreamsUrl(): string {
    return `${environment.api.externalRoot}/pipelinesDownstreams`;
  }

  static generateGetInvocationsUrl(): string {
    return `${environment.api.externalRoot}/pipelineInvocations`;
  }

  get(): Observable<Pipeline[]> {
    return this.http.get<Pipeline[]>(`${PipelinesService.generateGetUrl()}`);
  }

  getDownstreams(): Observable<PipelineDownstream[]> {
    return this.http.get<PipelineDownstream[]>(
      `${PipelinesService.generateGetDownstreamsUrl()}`
    );
  }

  getRequiredSchemas(pipelineId: number): Observable<SchemaDetails[]> {
    return this.http.get<SchemaDetails[]>(
      `${PipelinesService.generateGetUrl()}/${pipelineId}/schemas`
    );
  }

  getPipelineEdges(pipelineId: number): Observable<PipelineEdge[]> {
    return this.http.get<PipelineEdge[]>(
      `${PipelinesService.generateGetUrl()}/${pipelineId}/edges`
    );
  }

  getInvocations(): Observable<PipelineInvocation[]> {
    return this.http.get<PipelineInvocation[]>(
      `${PipelinesService.generateGetInvocationsUrl()}`
    );
  }

  getInvocationsByJobId(
    jobExecutionId: number
  ): Observable<PipelineInvocation[]> {
    return this.http.get<PipelineInvocation[]>(
      `${PipelinesService.generateGetInvocationsUrl()}?jobId=${jobExecutionId}`
    );
  }
}
