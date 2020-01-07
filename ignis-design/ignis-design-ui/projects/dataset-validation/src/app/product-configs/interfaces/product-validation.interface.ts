export type ValidationEvent = PipelineTask | ValidationComplete;

export enum TaskStatus {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED'
}

export enum TaskType {
  Pipeline = 'PIPELINE',
  PipelineStep = 'PIPELINE_STEP',
  PipelineGraph = 'PIPELINE_GRAPH',
  Complete = 'COMPLETE'
}

export interface ProductConfigTaskList {
  productId: number;
  pipelineTasks: {};
}

export interface ValidationTask {
  pipelineId: number;
  name: string;
  type: TaskType;
  status: TaskStatus;
  message: string;
  tasks?: ValidationTask[];
}

export interface ValidationComplete {
  type: TaskType.Complete;
  status: TaskStatus.SUCCESS;
  message: string;
}

export interface PipelineTask extends ValidationTask {
  type: TaskType.Pipeline;
  pipelineId: number;
  tasks: PipelineCheck[];
}

export interface PipelineCheck extends ValidationTask {
  pipelineId: number;
  pipelineStepId: number;
}
