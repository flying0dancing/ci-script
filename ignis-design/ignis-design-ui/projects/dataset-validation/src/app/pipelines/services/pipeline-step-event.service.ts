import { EventEmitter, Injectable, Output } from '@angular/core';
import { EditPipelineStepEvent } from '../interfaces/events.interfaces';

@Injectable()
export class PipelineStepEventService {
  @Output() editEvent = new EventEmitter<EditPipelineStepEvent>();
}
