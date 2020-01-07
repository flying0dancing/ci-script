import { ApiError } from '../../core/utilities/interfaces/errors.interface';
import { Field, Schema } from '../../schemas';
import { ScriptletInputInterface } from '../components/step-form/Scriptlet/pipeline-scriptlet-step-sub-form.component';

export enum TransformationType {
  MAP = 'MAP',
  AGGREGATION = 'AGGREGATION',
  JOIN = 'JOIN',
  WINDOW = 'WINDOW',
  UNION = 'UNION',
  SCRIPTLET = 'SCRIPTLET'
}

export type PipelineStep =
  | PipelineMapStep
  | PipelineAggregationStep
  | PipelineJoinStep
  | PipelineWindowStep
  | PipelineUnionMapStep
  | PipelineScriptletStep;

export type SingleSelectStep =
  | PipelineMapStep
  | PipelineAggregationStep
  | PipelineJoinStep
  | PipelineWindowStep;

export interface PipelineScriptletStep {
  id: number;
  name: string;
  description: string;
  jarName: string;
  className: string;
  schemaOutId: number;
  scriptletInputs: ScriptletInputInterface[];
  type: TransformationType.SCRIPTLET;
}

export interface PipelineMapStep {
  id: number;
  name: string;
  description: string;
  schemaInId: number;
  schemaOutId: number;
  selects: Select[];
  filters: string[];
  type: TransformationType.MAP;
}

export function addMissingOrderToSelects(selects: Select[]) {
  return selects.map((value, index) => {
    return { ...value, order: !!value.order ? value.order : index };
  });
}

export interface Union {
  selects: Select[];
  filters: string[];
}

export interface PipelineUnionMapStep {
  id: number;
  name: string;
  description: string;
  schemaOutId: number;
  unions: { [key: number]: Union };
  type: TransformationType.UNION;
}

export interface PipelineAggregationStep {
  id: number;
  name: string;
  description: string;
  schemaInId: number;
  schemaOutId: number;
  selects: Select[];
  filters: string[];
  groupings: string[];
  type: TransformationType.AGGREGATION;
}

export interface PipelineJoinStep {
  id: number;
  name: string;
  description: string;
  schemaOutId: number;
  selects: Select[];
  joins: JoinRequest[];
  type: TransformationType.JOIN;
}

export interface PipelineWindowStep {
  id: number;
  name: string;
  description: string;
  schemaInId: number;
  schemaOutId: number;
  selects: Select[];
  filters: string[];
  type: TransformationType.WINDOW;
}

export interface Select {
  select: string;
  outputFieldId: number;
  order: number;
  intermediateResult: boolean;
  hasWindow: boolean;
  window: Window | null;
}

export interface Window {
  partitionBy: string[];
  orderBy: Order[];
}

export interface Order {
  fieldName: string;
  direction: OrderDirection;
  priority: number;
}

export enum OrderDirection {
  ASC = 'ASC',
  DESC = 'DESC'
}

export enum JoinType {
  LEFT = 'LEFT',
  INNER = 'INNER',
  FULLOUTER = 'FULL_OUTER'
}

export interface Join {
  id: number;
  leftSchema: Schema;
  rightSchema: Schema;
  joinFields: JoinField[];
  joinType: JoinType;
}

export interface JoinRequest {
  id: number;
  leftSchemaId: number;
  rightSchemaId: number;
  joinFields: JoinFieldRequest[];
  joinType: JoinType;
}

export interface JoinField {
  leftField: Field;
  rightField: Field;
}

export interface JoinFieldRequest {
  leftFieldId: number;
  rightFieldId: number;
}

export interface SelectResult {
  outputFieldName: string;
  outputFieldId: number;
  unionSchemaId: number;
  errors: ApiError[];
  valid: boolean;
}

export interface SelectsExecutionErrors {
  transformationParseErrors: ApiError[];
  individualErrors: SelectResult[];
}

export interface StepExecutionResult {
  errors: ApiError[];
  selectsExecutionErrors: SelectsExecutionErrors;
}

export interface UpdatePipelineError {
  pipelineNotFoundError: ApiError;
  stepExecutionResult: StepExecutionResult;
  unexpectedError?: ApiError[];
}

export function isEmpty(updateError: UpdatePipelineError) {
  if (!updateError) {
    return true;
  }

  const noNotFoundError = !updateError.pipelineNotFoundError;
  if (!updateError.stepExecutionResult && noNotFoundError) {
    return true;
  }

  const noUnexpectedError = !updateError.unexpectedError || updateError.unexpectedError.length === 0;
  if (!updateError.stepExecutionResult && noUnexpectedError) {
    return true;
  }

  const noIndividualErrors =
    (!updateError.stepExecutionResult.selectsExecutionErrors.transformationParseErrors
      || updateError.stepExecutionResult.selectsExecutionErrors.transformationParseErrors.length === 0)
    && (!updateError.stepExecutionResult.selectsExecutionErrors.individualErrors
    || updateError.stepExecutionResult.selectsExecutionErrors.individualErrors.length === 0);

  const noStepExecutionErrors = !updateError.stepExecutionResult.errors || updateError.stepExecutionResult.errors.length === 0;

  return noNotFoundError && noUnexpectedError && noIndividualErrors && noStepExecutionErrors;
}

export interface SyntaxCheckRequest {
  sparkSql: string;
  outputFieldId: number;
  pipelineStep: PipelineStep;
}

export function toTopLevelFormErrors(updateError: UpdatePipelineError): ApiError[] {
  const errors = [];
  if (!updateError) {
    return errors;
  }

  if (!!updateError.pipelineNotFoundError) {
    errors.push(updateError.pipelineNotFoundError);
  }

  if (!!updateError.unexpectedError) {
    updateError.unexpectedError.forEach(error => errors.push(error));
  }

  if (!!updateError.stepExecutionResult && !!updateError.stepExecutionResult.errors) {
    updateError.stepExecutionResult.errors.forEach(err => errors.push(err));
  }

  return errors;
}
