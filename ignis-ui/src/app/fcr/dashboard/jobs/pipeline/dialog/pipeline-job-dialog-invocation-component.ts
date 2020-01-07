import { PipelineInvocation } from "@/core/api/pipelines/pipelines.interfaces";
import { Component, Input, OnInit } from "@angular/core";

@Component({
  selector: "app-pipeline-invocation-dialog",
  templateUrl: "./pipeline-job-dialog-invocation-component.html",
  styleUrls: ["./pipeline-job-dialog-invocation-component.scss"]
})
export class PipelineJobDialogInvocationComponent implements OnInit {
  @Input() pipelineInvocations: PipelineInvocation[];

  ngOnInit(): void {
    // no-op
  }
}
