export interface EditRuleEvent {
  id: number;
}

export interface DeleteRuleEvent {
  id: number;
  name: string;
  ref?: string;
}

export interface EditFieldEvent {
  id: number;
}
