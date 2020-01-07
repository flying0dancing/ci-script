import { EventEmitter, Injectable, Output } from '@angular/core';
import { EditRuleEvent } from '../interfaces/events.interfaces';

@Injectable()
export class RuleEventService {
  @Output() editEvent = new EventEmitter<EditRuleEvent>();
}
