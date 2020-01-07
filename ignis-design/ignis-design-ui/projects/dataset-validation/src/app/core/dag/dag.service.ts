import { Injectable } from '@angular/core';
import * as d3 from 'd3';
import * as d3Dag from 'd3-dag/index.js';
import * as d3Shapes from 'd3-shape';
import {
  DagEdge,
  DagElements,
  DagNode
} from './dag-display-elements.interface';
import {
  edgeLinkDataAccessor,
  edgeSourceAccessor,
  edgeTargetAccessor,
  InputDagEdge,
  DrawingContext
} from './dag-inputs.interface';

//Rotate dag by 90 degrees so flow goes from right to left
const DAG_ROTATION = -Math.PI / 2;

const COLOR_SCHEMA: (x: number) => string = d3.interpolateViridis;
const MIN_NODE_SPACING_PERCENTAGE = 1.2;

@Injectable({
  providedIn: 'root'
})
export class DagService {
  createGraph(
    edges: InputDagEdge[],
    drawingContext: DrawingContext
  ): DagElements {
    const nodeRadius = drawingContext.nodeRadius;
    const nodeSeparation = drawingContext.nodeSeparation;

    const dagConnectOperator = d3Dag.dagConnect();
    dagConnectOperator.sourceAccessor(edgeSourceAccessor);
    dagConnectOperator.targetAccessor(edgeTargetAccessor);
    dagConnectOperator.linkData(edgeLinkDataAccessor);

    const dagConnect = dagConnectOperator(edges);
    const dag = d3Dag.sugiyama().layering(d3Dag.layeringLongestPath())(
      dagConnect
    );

    const adjustedRadius = this.adjustIfOverlapping(
      dag,
      nodeRadius,
      nodeSeparation,
      drawingContext
    );

    const line = d3
      .line<any>()
      .curve(d3Shapes.curveCatmullRom)
      .x(vector => this.adjustPositionX(vector, nodeSeparation, drawingContext))
      .y(vector =>
        this.adjustPositionY(vector, nodeSeparation, drawingContext)
      );

    return {
      nodes: this.extractNodes(
        dag,
        nodeSeparation,
        adjustedRadius,
        drawingContext
      ),
      edges: this.extractEdges(dag, line),
      colorMap: this.createColorMap(dag),
      adjustmentScale: adjustedRadius / nodeRadius
    };
  }

  private recenterAroundOrigin(vector) {
    //d3Dag library creates a 1x1 grid wit all nodes, this method centers this grid symmetrically around (0,0)
    return {
      x: vector.x - 0.5,
      y: vector.y - 0.5
    };
  }

  private rotate(vector) {
    const x = vector.x;
    const y = vector.y;
    return {
      x: x * Math.cos(DAG_ROTATION) - y * Math.sin(DAG_ROTATION),
      y: x * Math.sin(DAG_ROTATION) + y * Math.cos(DAG_ROTATION)
    };
  }

  //If any nodes overlap we want to adjust the size of node radius so that they dont
  private adjustIfOverlapping(
    dag,
    nodeRadius: number,
    nodeSeparation: number,
    drawingContext: DrawingContext
  ): number {
    const nodes: any[] = dag.descendants();

    let maxOverlap;

    for (const i of nodes) {
      const iX = this.adjustPositionX(i, nodeSeparation, drawingContext);
      const iY = this.adjustPositionY(i, nodeSeparation, drawingContext);

      for (const j of nodes) {
        if (i.id === j.id) {
          continue;
        }

        const jX = this.adjustPositionX(j, nodeSeparation, drawingContext);
        const jY = this.adjustPositionY(j, nodeSeparation, drawingContext);

        const dx = Math.abs(iX - jX);
        const dy = Math.abs(iY - jY);
        const dr = Math.sqrt(dx * dx + dy * dy);

        if (dr < nodeRadius) {
          if (!!maxOverlap) {
            maxOverlap = Math.max(maxOverlap, dr);
          } else {
            maxOverlap = dr;
          }
        }
      }
    }

    if (maxOverlap) {
      const percentageOverlapping = (maxOverlap + nodeRadius) / nodeRadius;
      return nodeRadius / percentageOverlapping / MIN_NODE_SPACING_PERCENTAGE;
    }

    return nodeRadius;
  }

  private extractNodes(
    dag,
    nodeSeparation: number,
    nodeRadius: number,
    drawingContext: DrawingContext
  ) {
    const nodes: DagNode[] = [];
    const descendants: any[] = dag.descendants();

    for (const descendant of descendants) {
      nodes.push({
        id: descendant.id,
        x: this.adjustPositionX(descendant, nodeSeparation, drawingContext),
        y: this.adjustPositionY(descendant, nodeSeparation, drawingContext),
        radius: nodeRadius
      });
    }
    return nodes;
  }

  private adjustPositionY(
    descendant,
    nodeSeparation: number,
    drawingContext: DrawingContext
  ) {
    const adjustedY =
      this.rotate(this.recenterAroundOrigin(descendant)).y +
      drawingContext.yShift;
    return adjustedY * nodeSeparation * drawingContext.heightMultiplier;
  }

  private adjustPositionX(
    descendant,
    nodeSeparation: number,
    drawingContext: DrawingContext
  ) {
    const adjustedX =
      this.rotate(this.recenterAroundOrigin(descendant)).x +
      drawingContext.xShift;
    return adjustedX * nodeSeparation * drawingContext.widthMultiplier;
  }

  private createColorMap(dag) {
    const steps = dag.size();
    const colorMap = {};

    dag.each(
      (node, loopIndex) => (colorMap[node.id] = COLOR_SCHEMA(loopIndex / steps))
    );
    return colorMap;
  }

  private extractEdges(dag, line) {
    const links: any[] = dag.links();
    const dagEdges: DagEdge[] = [];

    for (const link of links) {
      const data = link.data;
      const source = link.source;
      const target = link.target;

      dagEdges.push({
        d: line(data.points),
        source: {
          id: source.id,
          x: source.x,
          y: source.y
        },
        target: {
          id: target.id,
          x: target.x,
          y: target.y
        }
      });
    }
    return dagEdges;
  }
}
