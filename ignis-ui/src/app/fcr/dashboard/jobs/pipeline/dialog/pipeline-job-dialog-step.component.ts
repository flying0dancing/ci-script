import { PipelineStepInvocation, PipelineStepStatus } from '@/core/api/pipelines/pipelines.interfaces';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: "app-pipeline-step-invocation-dialog",
  templateUrl: "./pipeline-job-dialog-step.component.html",
  styleUrls: ["./pipeline-job-dialog-step.component.scss"]
})
export class PipelineJobDialogStepComponent implements OnInit {
  @Input() pipelineStepInvocation: PipelineStepInvocation;
  public icon;
  public iconClass;
  public stepRunning;
  public PipelineStepStatus: typeof PipelineStepStatus = PipelineStepStatus;

  ngOnInit() {
    this.statusRenderer(this.pipelineStepInvocation.status);
  }

  private statusRenderer(value) {
    switch (value) {
      case PipelineStepStatus.RUNNING:
        this.stepRunning = true;
        break;
      case PipelineStepStatus.PENDING:
        this.stepRunning = false;
        this.icon = "access_time";
        this.iconClass = "pending";
        break;
      case PipelineStepStatus.FAILED:
        this.stepRunning = false;
        this.icon = "error";
        this.iconClass = "fail";
        break;
      default:
        this.stepRunning = false;
        this.icon = "check_circle";
        this.iconClass = "success";
    }
  }
}
