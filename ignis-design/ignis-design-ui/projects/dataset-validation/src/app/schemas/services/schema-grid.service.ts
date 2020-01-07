import { EventEmitter, Injectable, Output } from '@angular/core';

@Injectable()
export class SchemaGridEventService {
  @Output() selectEvent = new EventEmitter<SchemaSelectEvent>();
}

export interface SchemaSelectEvent {
  allSelectedIds: number[];
}
