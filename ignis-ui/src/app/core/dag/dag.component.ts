import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from "@angular/core";
import { EdgeContext, EdgeKey, NodeContext } from "./dag-context.interface";
import { DagEdge, DagNode } from "./dag-display-elements.interface";
import { DagEventService } from "./dag-event.service";
import { DrawingContext, InputDagEdge } from "./dag-inputs.interface";
import { DagService } from "./dag.service";

const GRAPH_PADDING = 10;
const DEFAULT_EDGE_WIDTH = 3.0;

@Component({
  selector: "app-dag",
  templateUrl: "dag.component.html",
  styleUrls: ["./dag.component.scss"]
})
export class DagComponent implements OnInit, OnChanges {
  private nodeSeparation = 100.0;
  private initialViewBox;
  viewBoxSize;

  mouseDown = false;
  dagEdges: DagEdge[];
  dagNodes: DagNode[];
  colorMap = {};

  @Input()
  edgeWidth = DEFAULT_EDGE_WIDTH;
  viewBoxLocation;

  @Input()
  zoom = 1.0;
  @Input()
  width = 100.0;
  @Input()
  height = 100.0;
  @Input()
  colorMapOverride: {};
  @Input()
  nodeRadius = 5.0;

  @Input()
  inputEdges: InputDagEdge[] = [];

  @Input()
  nodeContextMap: Map<number, NodeContext> = new Map<number, NodeContext>();

  @Input()
  edgeContextMap: Map<EdgeKey, EdgeContext> = new Map<EdgeKey, EdgeContext>();

  @Input()
  selectedEdge: number;

  @Input()
  showInvalidGraph = false;

  constructor(
    public dagService: DagService,
    public dagEventService: DagEventService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.generateGraph();

    if (changes.colorMapOverride) {
      this.handleColorChange();
    }
  }

  ngOnInit(): void {
    this.initViewBox();
    this.generateGraph();
    this.dagEventService.zoomEvent.subscribe(event => {
      this.zoom = event;
    });
    this.dagEventService.selectEdge.subscribe(edge => {
      this.selectedEdge = edge;
    });
  }

  private initViewBox() {
    this.initialViewBox = -1 * (this.nodeRadius + 50 + GRAPH_PADDING);
    this.viewBoxSize = 2 * this.nodeRadius + 100 + 2 * GRAPH_PADDING;
    this.viewBoxLocation = {
      x: this.initialViewBox,
      y: this.initialViewBox
    };
  }

  private generateGraph() {
    if (!this.inputEdges || this.inputEdges.length === 0) {
      return;
    }

    const drawingContext: DrawingContext = {
      widthMultiplier: this.width / this.height,
      heightMultiplier: 1,
      nodeRadius: this.nodeRadius,
      nodeSeparation: this.nodeSeparation,
      xShift: 0,
      yShift: 0
    };

    const dagElements = this.dagService.createGraph(
      this.inputEdges,
      drawingContext
    );
    this.dagEdges = dagElements.edges;
    this.dagNodes = dagElements.nodes;
    if (this.colorMapOverride) {
      this.colorMap = { ...dagElements.colorMap, ...this.colorMapOverride };
    } else {
      this.colorMap = dagElements.colorMap;
    }
    this.edgeWidth = this.edgeWidth * dagElements.adjustmentScale;
  }

  handleColorChange() {
    if (!!this.colorMapOverride) {
      this.colorMap = Object.assign({}, this.colorMapOverride);
    } else {
      this.colorMap = Object.assign({}, this.colorMap);
    }
  }

  getTooltip(node: DagNode): string {
    const nodeContext = this.nodeContextMap[node.id];
    return nodeContext ? nodeContext.tooltip : null;
  }

  getEdgeTooltip(edge: DagEdge): string {
    const key = edge.source.id + "-" + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];
    return edgeContext == null ? "" : edgeContext.tooltip;
  }

  handleEdgeClick(edge: DagEdge) {
    const key = edge.source.id + "-" + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];

    if (this.showInvalidGraph || (edgeContext && edgeContext.valid)) {
      this.dagEventService.edgeClickEvent.emit(edgeContext.id);
      this.dagEventService.selectEdge.emit(edgeContext.id);
    }
  }

  mouseMove($event: MouseEvent) {
    if (this.mouseDown) {
      this.viewBoxLocation = {
        x: this.viewBoxLocation.x - $event.movementX / 2,
        y: this.viewBoxLocation.y - $event.movementY / 2
      };
    }
  }

  isEdgeSelected(edge: DagEdge) {
    const key = edge.source.id + "-" + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];
    if (!edgeContext) {
      return false;
    }
    return edgeContext.id === this.selectedEdge;
  }

  isEdgeValid(edge: DagEdge) {
    const key = edge.source.id + "-" + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];
    return this.showInvalidGraph || (edgeContext && edgeContext.valid);
  }

  isNodeValid(node: DagNode) {
    const key = node.id;
    const nodeContext: NodeContext = this.nodeContextMap[key];
    return this.showInvalidGraph || (nodeContext && nodeContext.valid);
  }
}
