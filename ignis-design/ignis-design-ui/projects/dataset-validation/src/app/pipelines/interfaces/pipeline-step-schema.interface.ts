export class PipelineStepSchema {
  schemaId: number;
  displayName: string;
  physicalTableName: string;
  majorVersion: number;

  constructor(
    schemaId: number,
    displayName: string,
    physicalTableName: string,
    majorVersion: number
  ) {
    this.schemaId = schemaId;
    this.displayName = displayName;
    this.physicalTableName = physicalTableName;
    this.majorVersion = majorVersion;
  }
}
