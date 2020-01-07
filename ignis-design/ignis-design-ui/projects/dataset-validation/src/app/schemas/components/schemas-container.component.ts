import {
  ChangeDetectionStrategy,
  Component,
  Input,
  NgZone,
  OnDestroy,
  OnInit
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs';
import { Schema } from '..';
import { ShortcutsService } from '../../core/shortcuts/shortcuts.service';
import * as schemas from '../index';
import { OpenNewSchemaVersionDialog } from '../interfaces/event.interfaces';
import { SchemaEventService } from '../services/schema-event.service';
import { CreateSchemaDialogComponent, Mode } from './contained/create-schema-dialog.component';
import { CreateNewVersionDialogComponent } from './contained/dialog/create-new-version-dialog.component';

@Component({
  selector: 'dv-schema-container',
  templateUrl: './schemas-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SchemasContainerComponent implements OnInit, OnDestroy {
  @Input() productId: number;
  @Input() productName: string;

  @Input() loading: boolean;
  @Input() schemas: Schema[];

  newVersionSubscription: Subscription;
  copySubscription: Subscription;

  constructor(
    private store: Store<any>,
    private schemaEventService: SchemaEventService,
    private dialog: MatDialog,
    private shortcutsService: ShortcutsService,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    this.newVersionSubscription = this.schemaEventService.openNewSchemaVersionDialog.subscribe(
      newSchemaVersionEvent =>
        this.openNewSchemaVersionDialog(newSchemaVersionEvent)
    );
    this.copySubscription = this.schemaEventService.openCopySchemaDialog.subscribe(schemaId =>
      this.openCreateCopySchemaDialog({ mode: Mode.COPY }, schemaId));

    this.setupShortcuts();
  }

  private setupShortcuts() {
    this.shortcutsService.keepCheatSheetShortcutsOnly();
  }

  ngOnDestroy() {
    this.newVersionSubscription.unsubscribe();
    this.copySubscription.unsubscribe();
  }

  addSchema(): void {
    this.openCreateCopySchemaDialog({ mode: Mode.CREATE });
  }

  private openCreateCopySchemaDialog(data, copySchemaId = null): void {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(CreateSchemaDialogComponent, {
        width: '420px',
        maxWidth: '150vw',
        height: 'auto',
        maxHeight: '90vh',
        disableClose: true,
        data
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result && result.mode === Mode.COPY) {
          this.store.dispatch(new schemas.Copy(this.productId, copySchemaId, result.schema));

        } else if (result && result.mode === Mode.CREATE) {
          this.store.dispatch(new schemas.Create(this.productId, result.schema));
        }

        dialogRef = undefined;
      });
    });
  }

  private openNewSchemaVersionDialog(event: OpenNewSchemaVersionDialog) {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(CreateNewVersionDialogComponent, {
        width: '420px',
        height: 'auto',
        disableClose: true,
        data: {
          schemaId: event.schemaId,
          schemaDisplayName: event.schemaDisplayName,
          previousStartDate: event.previousStartDate
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.store.dispatch(
            new schemas.CreateNewVersion(
              this.productId,
              result.id,
              result.startDate
            )
          );
        }
        dialogRef = undefined;
      });
    });
  }
}
