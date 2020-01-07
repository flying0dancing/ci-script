import {
  EdgeContext,
  EdgeKey,
  NodeContext
} from "@/core/dag/dag-context.interface";
import { InputDagEdge } from "@/core/dag/dag-inputs.interface";
import { StageJobFormService } from "@/fcr/dashboard/jobs/stage-job-form.service";
import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "app-downstream-pipeline-graph",
  templateUrl: "./downstream-pipeline-graph.component.html",
  styleUrls: ["./downstream-pipeline-graph.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [StageJobFormService]
})
export class DownstreamPipelineGraphComponent {
  @Input()
  zoom = 1.0;
  @Input()
  width = 800.0;
  @Input()
  height = 300.0;
  @Input()
  colorMapOverride;
  @Input()
  inputEdges: InputDagEdge[][] = [];
  @Input()
  nodeContextMap: Map<number, NodeContext> = new Map<number, NodeContext>();
  @Input()
  isPipelineGraphLoading;

  @Input()
  edgeContextMap: Map<EdgeKey, EdgeContext> = new Map<EdgeKey, EdgeContext>();

  dagZoom = 1.0;
  minDagZoom = 0.9;
  maxDagZoom = 3.0;

  mousewheelOnDagContainer($event: WheelEvent) {
    $event.preventDefault();

    const zoomDirection = Math.sign($event.deltaY);
    if (zoomDirection > 0 && this.dagZoom > this.maxDagZoom) {
      return;
    }

    if (zoomDirection < 0 && this.dagZoom < this.minDagZoom) {
      return;
    }
    this.dagZoom += 0.1 * zoomDirection;
  }
}
