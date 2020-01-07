import { SchemaDetails } from "@/core/api/pipelines/pipelines.interfaces";

export interface DownstreamPipeline {
  pipelineId: number;
  pipelineName: string;
  requiredSchemas: SchemaDetails[];
}
