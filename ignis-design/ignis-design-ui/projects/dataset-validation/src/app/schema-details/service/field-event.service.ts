import { EventEmitter, Injectable, Output } from '@angular/core';
import { EditFieldEvent } from '../interfaces/events.interfaces';

@Injectable()
export class FieldEventService {
  @Output() editEvent = new EventEmitter<EditFieldEvent>();
}
