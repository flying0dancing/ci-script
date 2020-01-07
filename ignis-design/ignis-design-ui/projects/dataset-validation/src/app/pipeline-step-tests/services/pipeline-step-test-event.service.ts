import { EventEmitter, Injectable, Output } from '@angular/core';
import { EditPipelineStepTestEvent } from '../interfaces/pipeline-step-test-events';

@Injectable({
  providedIn: 'root'
})
export class PipelineStepTestEventService {
  @Output() editEvent = new EventEmitter<EditPipelineStepTestEvent>();
}
