import * as mock from '@/core/api/datasets/_tests/datasets.mocks';
import { PipelineDownstream, SchemaDetails } from '@/core/api/pipelines/pipelines.interfaces';
import { EligiblePipeline } from '@/core/api/staging/staging.interfaces';
import { TestBed } from '@angular/core/testing';

import { JobsHelperService } from '../jobs-helper.service';

describe("JobsHelperService", () => {
  let service: any;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [JobsHelperService]
    });

    service = TestBed.get(JobsHelperService);
  });

  describe("filterSchemas", () => {
    it("should filter schemas by name", () => {
      const schemas = [
        { displayName: "aa" },
        { displayName: "ab" },
        { displayName: "ABC" }
      ];

      expect(service.filterSchemas("AB", schemas)).toEqual([
        { displayName: "ab" },
        { displayName: "ABC" }
      ]);
    });
  });

  describe("filterSchemasByNameAndReferenceDate", () => {
    it("should filter schemas by name and reference date", () => {
      const schemas = [
        { displayName: "aa", startDate: "2018-01-01", endDate: "2018-01-02" },
        { displayName: "aab", startDate: "2000-01-01", endDate: null },
        { displayName: "ab", startDate: "2000-01-01", endDate: null },
        { displayName: "ab", startDate: "1970-01-01", endDate: "1999-12-31" },
        { displayName: "ABC", startDate: "2018-01-01", endDate: "2018-02-01" }
      ];

      expect(
        service.filterSchemasByNameAndReferenceDate(
          "AB",
          new Date("2000-01-21"),
          schemas
        )
      ).toEqual([
        { displayName: "aab", startDate: "2000-01-01", endDate: null },
        { displayName: "ab", startDate: "2000-01-01", endDate: null }
      ]);
    });
  });

  describe("filterSourceFiles", () => {
    it("should filter source files", () => {
      const sourceFiles = ["aa", "ab", "ABC"];

      expect(service.filterSourceFiles("AB", sourceFiles)).toEqual([
        "ab",
        "ABC"
      ]);
    });

    it("should handle null", () => {
      const sourceFiles = ["aa", "ab", "ABC"];

      expect(service.filterSourceFiles(null, sourceFiles)).toEqual(sourceFiles);
    });
  });

  describe("toEligiblePipeline", () => {
    const schema: SchemaDetails =
      { id: 100, displayName: 'My Schema', physicalTableName: 'MY_SCHEMA', version: 1 };

    const pipeline: PipelineDownstream = {
      pipelineId: 123456,
      pipelineName: 'My Pipeline',
      requiredSchemas: [schema]
    };

    it("should return non-eligible pipeline when there are no staging items", () => {
      const stagingItems = [];
      const datasets = [mock.dataset];

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['My Schema'],
        enabled: false
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-01-01', stagingItems, datasets, pipeline))
        .toEqual(expected);
    });

    it("should return eligible pipeline with single required schema provided by staging item", () => {
      const downstreamPipeline = { ...pipeline, requiredSchemas: [ { ...schema, id: 100 }]};
      const datasets = [mock.dataset];

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['My Schema'],
        enabled: true
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-09-01', [100], datasets, downstreamPipeline))
        .toEqual(expected);
    });

    it("should return eligible pipeline when multiple schemas required", () => {
      const datasets = [mock.dataset];
      const downstreamPipeline = {
        ...pipeline,
        requiredSchemas: [
          { ...schema, id: 100, displayName: 'Schema 100' },
          { ...schema, id: 101, displayName: 'Schema 101' },
          { ...schema, id: 102, displayName: 'Schema 102' }]
      };

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['Schema 100', 'Schema 101', 'Schema 102'],
        enabled: true
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-09-01', [100, 101, 102], datasets, downstreamPipeline))
        .toEqual(expected);
    });

    it("should return non-eligible pipeline when required schema is missing from staging items and datasets", () => {
      const datasets = [mock.dataset];
      const downstreamPipeline = {
        ...pipeline,
        requiredSchemas: [
          { ...schema, id: 100, displayName: 'Schema 100' },
          { ...schema, id: 101, displayName: 'Schema 101' },
          { ...schema, id: 102, displayName: 'Schema 102' }]
      };

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['Schema 100', 'Schema 101', 'Schema 102'],
        enabled: false
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-09-01', [100, 999, 102], datasets, downstreamPipeline))
        .toEqual(expected);
    });

    it("should return eligible pipeline when required schema is missing from staging items but found in datasets", () => {
      const datasets = [{
        ...mock.dataset,
        entityCode: 'myEntityCode',
        localReferenceDate: '2019-09-01',
        tableId: 101
      }];
      const downstreamPipeline = {
        ...pipeline,
        requiredSchemas: [
          { ...schema, id: 100, displayName: 'Schema 100' },
          { ...schema, id: 101, displayName: 'Schema 101' },
          { ...schema, id: 102, displayName: 'Schema 102' }]
      };

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['Schema 100', 'Schema 101', 'Schema 102'],
        enabled: true
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-09-01', [100, 999, 102], datasets, downstreamPipeline))
        .toEqual(expected);
    });

    it("should return non-eligible pipeline when required schemas entity code do not match", () => {
      const datasets = [{
        ...mock.dataset,
        tableId: 102,
        entityCode: 'myOtherEntityCode',
        localReferenceDate: '2019-09-01'
      }];
      const downstreamPipeline = {
        ...pipeline,
        requiredSchemas: [
          { ...schema, id: 100, displayName: 'Schema 100' },
          { ...schema, id: 101, displayName: 'Schema 101' },
          { ...schema, id: 102, displayName: 'Schema 102' }]
      };

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['Schema 100', 'Schema 101', 'Schema 102'],
        enabled: false
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-09-01', [100, 101], datasets, downstreamPipeline))
        .toEqual(expected);
    });

    it("should return non-eligible pipeline when required schemas reference dates do not match", () => {
      const datasets = [{
        ...mock.dataset,
        tableId: 102,
        entityCode: 'myEntityCode',
        localReferenceDate: '2019-12-31'
      }];
      const downstreamPipeline = {
        ...pipeline,
        requiredSchemas: [
          { ...schema, id: 100, displayName: 'Schema 100' },
          { ...schema, id: 101, displayName: 'Schema 101' },
          { ...schema, id: 102, displayName: 'Schema 102' }]
      };

      const expected: EligiblePipeline = {
        pipelineId: 123456,
        pipelineName: 'My Pipeline',
        requiredSchemas: ['Schema 100', 'Schema 101', 'Schema 102'],
        enabled: false
      };

      expect(service.toEligiblePipeline('myEntityCode', '2019-09-01', [100, 101], datasets, downstreamPipeline))
        .toEqual(expected);
    });
  });
});
