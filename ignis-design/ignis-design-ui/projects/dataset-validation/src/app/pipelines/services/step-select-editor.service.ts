import { EventEmitter, Injectable, Output } from '@angular/core';
import { SelectResult } from '../interfaces/pipeline-step.interface';

@Injectable({ providedIn: 'root' })
export class StepSelectEditorService {
  @Output()
  public selectValueChange: EventEmitter<SelectEditorChange> = new EventEmitter();
  public checkSyntax: EventEmitter<CheckSyntaxEvent> = new EventEmitter();
  public checkSyntaxSuccess: EventEmitter<SelectResult> = new EventEmitter();
}

export interface SelectEditorChange {
  fieldIndex: number;
  value: string;
  unionSchemaIn?: number;
}

export interface CheckSyntaxEvent {
  fieldId: number;
  sparkSql: string;
}
