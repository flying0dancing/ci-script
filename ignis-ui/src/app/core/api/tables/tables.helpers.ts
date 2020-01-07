import { Table } from "./tables.interfaces";

export function sortTableByPhysicalTableName(tables: Table[]): Table[] {
  return [...tables].sort((a, b) => {
    const nameA = a.physicalTableName.toUpperCase();
    const nameB = b.physicalTableName.toUpperCase();

    return nameA < nameB ? -1 : nameA > nameB ? 1 : 0;
  });
}
