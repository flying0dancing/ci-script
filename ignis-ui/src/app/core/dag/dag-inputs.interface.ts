export interface InputDagEdge {
  source: number;
  target: number;
}

export interface DrawingContext {
  widthMultiplier: number;
  heightMultiplier: number;
  nodeRadius: number;
  nodeSeparation: number;
  xShift?: number;
  yShift?: number;
}

export function edgeSourceAccessor(edge: InputDagEdge) {
  return edge.source;
}

export function edgeTargetAccessor(edge: InputDagEdge) {
  return edge.target;
}

export function edgeLinkDataAccessor(edge: InputDagEdge) {
  return [edgeSourceAccessor(edge), edgeTargetAccessor(edge)];
}
