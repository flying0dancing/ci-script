import { EventEmitter, Injectable, Output } from "@angular/core";

@Injectable({
  providedIn: "root"
})
export class DagEventService {
  @Output() edgeClickEvent = new EventEmitter<number>();
  @Output() selectEdge = new EventEmitter<number>();
  @Output() zoomEvent = new EventEmitter<number>();
}
