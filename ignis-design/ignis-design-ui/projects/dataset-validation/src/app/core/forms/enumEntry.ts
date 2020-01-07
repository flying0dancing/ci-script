export interface EnumEntry {
  label: string;
  value: string;
}

export function toEnumEntries<T>(object: T): EnumEntry[] {
  return Object.keys(object).map(enumEntry => {
    return {
      label: enumEntry,
      value: object[enumEntry]
    };
  });
}
