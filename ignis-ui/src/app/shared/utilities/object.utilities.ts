import { isArray } from "./array.utilities";

export const isObject = (value: any): boolean =>
  typeof value === "object" && value !== null && !isArray(value);
