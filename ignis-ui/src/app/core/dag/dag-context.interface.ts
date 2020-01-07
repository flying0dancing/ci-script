export interface NodeContext {
  tooltip: string;
  valid: boolean;
}

// String of format source-target
export type EdgeKey = string;

export interface EdgeContext {
  id: number;
  tooltip: string;
  valid: boolean;
}
