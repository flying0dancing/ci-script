import { DatasetsTypes } from '@/core/api/datasets/';
import { Dataset } from '@/core/api/datasets/datasets.interfaces';
import { Pipeline, PipelineDownstream } from '@/core/api/pipelines/pipelines.interfaces';
import { EligiblePipeline } from '@/core/api/staging/staging.interfaces';
import { TablesInterfaces } from '@/core/api/tables/';
import { Table } from '@/core/api/tables/tables.interfaces';
import { Injectable } from '@angular/core';

@Injectable()
export class JobsHelperService {
  private static isRefDateContainedInPeriod(
    refDate: Date,
    schema: Table
  ): boolean {
    const startDate = new Date(schema.startDate);
    startDate.setHours(0, 0, 0);
    const afterStart = startDate <= refDate;

    let endDate;
    let beforeEnd = true;

    if (schema.endDate) {
      endDate = new Date(schema.endDate);
      endDate.setHours(0, 0, 0);
      beforeEnd = endDate >= refDate;
    }
    return afterStart && beforeEnd;
  }

  filterSchemas(
    schemaName: string,
    schemas: TablesInterfaces.Table[]
  ): TablesInterfaces.Table[] {
    if (!schemaName) {
      return [];
    }
    return schemas.filter(schema => {
      return (
        schema.displayName
          .trim()
          .toLowerCase()
          .indexOf(schemaName.trim().toLowerCase()) !== -1
      );
    });
  }

  filterPipelines(pipelineName: string, pipelines: Pipeline[]): Pipeline[] {
    if (!pipelineName) {
      return [];
    }
    return pipelines.filter(pipeline => {
      return (
        pipeline.name
          .trim()
          .toLowerCase()
          .indexOf(pipelineName.trim().toLowerCase()) !== -1
      );
    });
  }

  filterSchemasByNameAndReferenceDate(
    schemaName: string,
    refDate: Date,
    schemas: TablesInterfaces.Table[]
  ): TablesInterfaces.Table[] {
    return this.filterSchemas(schemaName, schemas).filter(schema =>
      JobsHelperService.isRefDateContainedInPeriod(refDate, schema)
    );
  }

  filterSchemasByReferenceDate(
    refDate: Date,
    schemas: TablesInterfaces.Table[]): TablesInterfaces.Table[] {

    return schemas.filter(schema =>
      JobsHelperService.isRefDateContainedInPeriod(refDate, schema)
    );
  }

  filterSourceFiles(
    value: string,
    sourceFiles: DatasetsTypes.SourceFiles
  ): DatasetsTypes.SourceFiles {
    if (value == null) {
      return sourceFiles;
    }
    return sourceFiles.filter(
      file => file.toLowerCase().indexOf(value.toLowerCase()) !== -1
    );
  }

  toUniqueSchemaNames(schemas: TablesInterfaces.Table[]): string[] {
    const uniqueSchemaNames = new Set();
    schemas.forEach(schema => uniqueSchemaNames.add(schema.displayName));

    const schemaNames = [];
    uniqueSchemaNames.forEach(uniqueSchemaName =>
      schemaNames.push(uniqueSchemaName)
    );

    return schemaNames;
  }

  toEligiblePipeline(
    entityCode: string,
    referenceDate: string,
    stagingItemsSchemaIds,
    datasets: Dataset[],
    downstreamPipeline: PipelineDownstream
  ): EligiblePipeline {

    const eligiblePipeline = {
      pipelineId: downstreamPipeline.pipelineId,
      pipelineName: downstreamPipeline.pipelineName,
      requiredSchemas: downstreamPipeline.requiredSchemas.map(schema => schema.displayName)
    };

    const stagingItemsWithRequiredSchemas = stagingItemsSchemaIds
      .filter(schemaId => downstreamPipeline.requiredSchemas.find(schema => schemaId === schema.id));

    const datasetsWithRequiredSchemaEntityCodeRefDate = datasets
      .filter(dataset => dataset.entityCode === entityCode && dataset.localReferenceDate === referenceDate)
      .filter(dataset => downstreamPipeline.requiredSchemas.find(schema => dataset.tableId === schema.id))
      .map(dataset => dataset.tableId);

    let requiredDatasetsFound = 0;

    const possibleDatasets =
      stagingItemsWithRequiredSchemas.concat(datasetsWithRequiredSchemaEntityCodeRefDate);

    for (const requiredSchema of downstreamPipeline.requiredSchemas) {
      const datasetSchemas = possibleDatasets.filter(dataset => dataset === requiredSchema.id);

      if (datasetSchemas.length > 0) {
        requiredDatasetsFound++;
      }
    }

    if (requiredDatasetsFound === downstreamPipeline.requiredSchemas.length) {
      return { ...eligiblePipeline, enabled: true };
    }

    return { ...eligiblePipeline, enabled: false, };
  }
}
