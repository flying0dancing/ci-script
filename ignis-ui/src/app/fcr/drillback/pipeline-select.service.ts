import { PipelineInvocationSelectEvent } from "@/fcr/drillback/drillback.interfaces";
import { EventEmitter, Injectable, Output } from "@angular/core";

@Injectable({ providedIn: "root" })
export class PipelineSelectService {
  @Output() public pipelineInvocationSelectEvent: EventEmitter<
    PipelineInvocationSelectEvent
  > = new EventEmitter();
}
