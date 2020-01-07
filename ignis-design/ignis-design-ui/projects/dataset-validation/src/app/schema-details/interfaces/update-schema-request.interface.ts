export class UpdateSchemaRequest {
  displayName: string;
  physicalTableName: string;
  startDate: string;
  endDate: string;

  constructor(
    displayName: string,
    physicalTableName: string,
    startDate: string,
    endDate: string
  ) {
    this.displayName = displayName;
    this.physicalTableName = physicalTableName;
    this.startDate = startDate;
    this.endDate = endDate;
  }
}
