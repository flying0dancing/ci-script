import { Pageable } from '@/core/api/common/pageable.interface';
import { StagingJobId } from '@/core/api/staging/staging.types';
import { HttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { StagingService } from '../staging.service';
import { dataset, getJobsResponse, jobId, stagingJob, validationJobRequest } from './staging.mocks';

describe("StagingService", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: StagingService;
  const jobExecutionId: StagingJobId = 1;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [StagingService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(StagingService);
  });

  describe("getJobs", () => {
    it("should get page of jobs", () => {
      const expected = getJobsResponse;

      let actual;

      const pageable: Pageable = {
        page: 123,
        size: 456,
        sort: {
          property: 'myproperty',
          direction: 'desc'
        }
      };

      service.getJobs(pageable).subscribe(response => (actual = response));

      const req = httpMock.expectOne(
        `${StagingService.jobsUrl()}?page=123&size=456&sort=myproperty,desc`
      );

      req.flush(getJobsResponse);

      httpMock.verify();

      expect(actual).toEqual(expected);
    });

    it("should get jobs filtered", () => {
      const expected = getJobsResponse;

      let actual;

      const pageable: Pageable = {
        page: 123,
        size: 456,
        sort: {
          property: 'myproperty',
          direction: 'desc'
        }
      };

      const filter = {
        expressionType: 'simple',
        columnName: 'myproperty',
        type: 'equals',
        filter: 'myvalue'
      };

      service.getJobs(pageable, filter).subscribe(response => (actual = response));

      const req = httpMock.expectOne(
        `${StagingService.jobsUrl()}?page=123&size=456&sort=myproperty,desc&search=${encodeURI(JSON.stringify(filter))}`
      );

      req.flush(getJobsResponse);

      httpMock.verify();

      expect(actual).toEqual(expected);
    });
  });

  it("should post a new staging job", () => {
    const expected = stagingJob;

    let actual;

    service
      .startStagingJob(stagingJob)
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(StagingService.jobsV2Url() + "?type=staging");

    req.flush(stagingJob);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should start a new validation job", () => {
    const expected = jobId;

    let actual;

    service
      .startValidationJob(validationJobRequest)
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(
      StagingService.jobsUrl() + "?type=validation"
    );

    req.flush(jobId);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should get datasets for a specified stagingJobId", () => {
    const expected = [dataset];

    let actual;

    service
      .getJobDatasets({jobExecutionId: jobExecutionId})
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(
      'https://localhost:8443/fcrengine/api/v1/stagingItems/?jobId=1'
    );

    req.flush([dataset]);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should get datasets for a specified datasetId", () => {
    const expected = [dataset];

    let actual;

    service
      .getJobDatasets({datasetId: 123})
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(
      'https://localhost:8443/fcrengine/api/v1/stagingItems/?datasetId=123'
    );

    req.flush([dataset]);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should put to stop a specified job from running", () => {
    let actual;

    service.stopJob(jobExecutionId).subscribe(response => (actual = response));

    const req = httpMock.expectOne(
      `${StagingService.stopJobByIdUrl(jobExecutionId)}`
    );

    req.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });
});
