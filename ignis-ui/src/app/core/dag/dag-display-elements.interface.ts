export interface DagEdgePoint {
  id: any;
  x: number;
  y: number;
}

export interface DagNode {
  id: any;
  radius: number;
  x: number;
  y: number;
}

export interface DagEdge {
  d: string;
  source: DagEdgePoint;
  target: DagEdgePoint;
}

export interface DagElements {
  edges: DagEdge[];
  nodes: DagNode[];
  colorMap: {};
  adjustmentScale: number | null;
}
