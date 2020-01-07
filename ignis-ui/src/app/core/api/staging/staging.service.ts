import { Pageable, toUrlParams } from '@/core/api/common/pageable.interface';
import { StartPipelineJobRequest } from '@/core/api/pipelines/pipelines.interfaces';
import { StagingInterfaces } from '@/core/api/staging/index';
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

import {
  GetDatasetsByJobResponse,
  GetJobsResponse,
  JobExecution,
  JobId,
  StagingJobsBody,
  ValidationJobRequest
} from './staging.interfaces';
import * as StagingTypes from './staging.types';

@Injectable()
export class StagingService {
  constructor(private http: HttpClient) {}

  static stagingDatasetsUrl(): string {
    return `${environment.api.externalRoot}/stagingItems`;
  }

  static jobsUrl(): string {
    return `${environment.api.externalRoot}/jobs`;
  }

  static jobsV2Url(): string {
    return `${environment.api.externalRootV2}/jobs`;
  }

  static getJobByIdUrl(jobExecutionId: StagingTypes.StagingJobId): string {
    return `${StagingService.jobsUrl()}/${jobExecutionId}`;
  }

  static stopJobByIdUrl(jobExecutionId: StagingTypes.StagingJobId): string {
    return `${StagingService.getJobByIdUrl(jobExecutionId)}/stop`;
  }
  static getStagingDatasetsUrl(params: StagingInterfaces.GetDatasetsParameters): string {
    const urlParams = [];

    if (params.jobExecutionId) {
      urlParams.push(`jobId=${params.jobExecutionId}`);
    }

    if (params.datasetId) {
      urlParams.push(`datasetId=${params.datasetId}`);
    }

    return `${StagingService.stagingDatasetsUrl()}/?${urlParams.join("&")}`;
  }

  startStagingJob(body: StagingJobsBody): Observable<Response> {
    return this.http.post<Response>(
      StagingService.jobsV2Url() + "?type=staging",
      body,
      { withCredentials: true }
    );
  }

  startValidationJob(body: ValidationJobRequest): Observable<JobId> {
    return this.http.post<JobId>(
      StagingService.jobsUrl() + "?type=validation",
      body,
      { withCredentials: true }
    );
  }

  startPipelineJob(body: StartPipelineJobRequest): Observable<JobId> {
    return this.http.post<JobId>(
      StagingService.jobsUrl() + "?type=pipeline",
      body,
      { withCredentials: true }
    );
  }

  getJobs(pageable: Pageable, filter?: any): Observable<GetJobsResponse> {
    const page = `?${toUrlParams(pageable)}`;
    const search = filter ? `&search=${encodeURI(JSON.stringify(filter))}` : '';

    return this.http.get<GetJobsResponse>(
      `${StagingService.jobsUrl()}${page}${search}`,
      { withCredentials: true }
    );
  }

  getJob(jobId: number): Observable<JobExecution> {
    return this.http.get<JobExecution>(
      StagingService.getJobByIdUrl(jobId),
      { withCredentials: true }
    );
  }

  getJobDatasets(
    params: StagingInterfaces.GetDatasetsParameters
  ): Observable<GetDatasetsByJobResponse> {
    return this.http.get<GetDatasetsByJobResponse>(
      StagingService.getStagingDatasetsUrl(params),
      { withCredentials: true }
    );
  }

  stopJob(jobExecutionId: StagingTypes.StagingJobId): Observable<Response> {
    return this.http.put<Response>(
      StagingService.stopJobByIdUrl(jobExecutionId),
      { withCredentials: true }
    );
  }
}
