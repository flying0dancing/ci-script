import { EligiblePipeline } from "@/core/api/staging/staging.interfaces";
import { StageJobFormService } from "@/fcr/dashboard/jobs/stage-job-form.service";
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from "@angular/core";
import { BehaviorSubject, Observable, pipe } from "rxjs";

@Component({
  selector: "app-stage-job-pipelines",
  templateUrl: "./stage-job-pipelines.component.html",
  styleUrls: ["./stage-job-pipelines.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [StageJobFormService]
})
export class StageJobPipelinesComponent implements OnInit {
  @Input() eligiblePipelines: EligiblePipeline[];

  @Input() selectedPipelines$: Observable<EligiblePipeline[]>;

  @Output() selectedEligiblePipelines: EventEmitter<
    EligiblePipeline[]
  > = new EventEmitter();

  @Output() clickedPipelines: EventEmitter<
    EligiblePipeline
  > = new EventEmitter();

  listSelectedPipelines: EligiblePipeline[] = [];

  isWithinGraphIcon = false;

  clickedPipelineId;

  ngOnInit(): void {
    this.selectedPipelines$.subscribe(pipelines => {
      this.listSelectedPipelines = [];
      pipelines.forEach(pipeline => this.selectPipeline(pipeline));
    });
  }

  viewPipelineGraph(pipeline: EligiblePipeline) {
    this.clickedPipelineId = pipeline.pipelineId;
    this.clickedPipelines.emit(pipeline);
  }

  selectPipeline(pipeline: EligiblePipeline) {
    if (!this.isWithinGraphIcon && pipeline.enabled) {
      if (this.listSelectedPipelines.includes(pipeline)) {
        const index = this.listSelectedPipelines.indexOf(pipeline, 0);
        if (index > -1) {
          this.listSelectedPipelines.splice(index, 1);
        }
      } else {
        this.listSelectedPipelines.push(pipeline);
      }
    }
    this.selectedEligiblePipelines.emit(this.listSelectedPipelines);
  }

  changePipelineSelection(pipeline: EligiblePipeline): string {
    if (!pipeline.enabled) {
      return "disabled-pipeline";
    } else {
      if (this.listSelectedPipelines.includes(pipeline)) {
        return "selected-pipeline";
      } else {
        return "unselected-pipeline";
      }
    }
  }

  changeButtonHighlight(pipeline: EligiblePipeline): string {
    if (pipeline.pipelineId === this.clickedPipelineId) {
      return "pipeline-button-selected";
    } else {
      return "pipeline-button";
    }
  }

  graphIconEnter() {
    this.isWithinGraphIcon = true;
  }

  graphIconLeave() {
    this.isWithinGraphIcon = false;
  }
}
