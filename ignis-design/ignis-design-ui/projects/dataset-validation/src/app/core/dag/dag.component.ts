import {
  Component,
  Input,
  OnChanges,
  OnInit,
  SimpleChanges
} from '@angular/core';
import { EdgeContext, EdgeKey, NodeContext } from './dag-context.interface';
import { DagEdge, DagNode } from './dag-display-elements.interface';
import { DagEventService } from './dag-event.service';
import { DrawingContext, InputDagEdge } from './dag-inputs.interface';
import { DagService } from './dag.service';

const GRAPH_PADDING = 10;
const DEFAULT_EDGE_WIDTH = 2.0;

@Component({
  selector: 'dv-dag',
  templateUrl: 'dag.component.html',
  styleUrls: ['./dag.component.scss']
})
export class DagComponent implements OnInit, OnChanges {
  private nodeRadius = 5.0;
  private nodeSeparation = 100.0;
  private graphSeparation = 1.2;
  private viewBoxShift = this.nodeRadius + 50 + GRAPH_PADDING;
  viewBoxSize = 2 * this.nodeRadius + 100 + 2 * GRAPH_PADDING;

  mouseDown = false;
  dagEdges: DagEdge[];
  dagNodes: DagNode[];
  dagGeneratedColorMap = {};
  colorMap = {};

  edgeWidth = DEFAULT_EDGE_WIDTH;
  viewBoxLocation = {
    x: -1 * this.viewBoxShift,
    y: -1 * this.viewBoxShift
  };

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
  edgeContextMap: Map<EdgeKey, EdgeContext> = new Map<EdgeKey, EdgeContext>();

  selectedEdge: number;

  constructor(
    public dagService: DagService,
    public dagEventService: DagEventService
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.inputEdges) {
      this.generateGraph();
      this.calculateViewBox();
    }

    if (changes.colorMapOverride) {
      this.handleColorChange();
    }
  }

  ngOnInit(): void {
    this.generateGraph();
    this.calculateViewBox();

    this.dagEventService.zoomEvent.subscribe(event => {
      this.zoom = event;
    });
    this.dagEventService.selectEdge.subscribe(edge => {
      this.selectedEdge = edge;
    });
  }

  private calculateViewBox() {
    if (this.inputEdges.length === 0) {
      this.viewBoxLocation = {
        x: -1 * this.viewBoxShift,
        y: -1 * this.viewBoxShift
      };
    } else {
      const eachGraphWidth =
        (50 * this.graphSeparation * this.width) /
        (this.height * this.inputEdges.length);
      this.viewBoxLocation = {
        x:
          -1 * this.viewBoxShift +
          (this.inputEdges.length - 1) * eachGraphWidth,
        y: -1 * this.viewBoxShift
      };
    }
  }

  private generateGraph() {
    if (!this.inputEdges || this.inputEdges.length === 0) {
      return;
    }

    const drawingContext: DrawingContext = {
      widthMultiplier: this.width / (this.height * this.inputEdges.length),
      heightMultiplier: 1,
      nodeRadius: this.nodeRadius,
      nodeSeparation: this.nodeSeparation,
      xShift: 0,
      yShift: 0
    };

    this.dagEdges = [];
    this.dagNodes = [];
    let maxAdjustment = 0;

    for (let i = 0; i < this.inputEdges.length; i++) {
      const edgeSet = this.inputEdges[i];
      const edgeSetDrawingContext = {
        ...drawingContext,
        xShift: i * this.graphSeparation
      };

      const dagElements = this.dagService.createGraph(
        edgeSet,
        edgeSetDrawingContext
      );
      dagElements.edges.forEach(edge => this.dagEdges.push(edge));
      dagElements.nodes.forEach(node => this.dagNodes.push(node));

      for (const colorMapKey in dagElements.colorMap) {
        this.dagGeneratedColorMap[colorMapKey] =
          dagElements.colorMap[colorMapKey];
      }

      maxAdjustment = Math.max(maxAdjustment, dagElements.adjustmentScale);
    }
    this.handleColorChange();
    this.edgeWidth = DEFAULT_EDGE_WIDTH * maxAdjustment;
  }

  handleColorChange() {
    if (this.colorMapOverride) {
      this.colorMap = Object.assign({}, this.colorMapOverride);
    } else {
      this.colorMap = Object.assign({}, this.dagGeneratedColorMap);
    }
  }

  getTooltip(node: DagNode): string {
    const nodeContext = this.nodeContextMap[node.id];
    return nodeContext ? nodeContext.tooltip : null;
  }

  getEdgeTooltip(edge: DagEdge): string {
    const key = edge.source.id + '-' + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];
    return edgeContext == null ? '' : edgeContext.tooltip;
  }

  handleEdgeClick(edge: DagEdge) {
    const key = edge.source.id + '-' + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];

    this.dagEventService.edgeClickEvent.emit(edgeContext.id);
    this.dagEventService.selectEdge.emit(edgeContext.id);
  }

  mouseMove($event: MouseEvent) {
    if (this.mouseDown) {
      this.viewBoxLocation = {
        x: this.viewBoxLocation.x - $event.movementX / 2,
        y: this.viewBoxLocation.y - $event.movementY / 2
      };
    }
  }

  isSelected(edge: DagEdge) {
    const key = edge.source.id + '-' + edge.target.id;
    const edgeContext: EdgeContext = this.edgeContextMap[key];
    if (!edgeContext) {
      return false;
    }
    return edgeContext.id === this.selectedEdge;
  }
}
