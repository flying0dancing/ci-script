import { EventEmitter, Injectable, Output } from '@angular/core';
import {
  EditSchemaEvent,
  NewSchemaVersion,
  OpenNewSchemaVersionDialog
} from '../interfaces/event.interfaces';

@Injectable({providedIn: 'root'})
export class SchemaEventService {
  @Output() editEvent = new EventEmitter<EditSchemaEvent>();
  @Output() newSchemaVersion = new EventEmitter<NewSchemaVersion>();
  @Output() openNewSchemaVersionDialog = new EventEmitter<OpenNewSchemaVersionDialog>();
  @Output() openCopySchemaDialog = new EventEmitter<number>();
}
