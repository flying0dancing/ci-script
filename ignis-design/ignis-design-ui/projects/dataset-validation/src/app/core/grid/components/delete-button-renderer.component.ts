import { ChangeDetectionStrategy, Component, NgZone } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { ConfirmDialogComponent } from '../../dialogs/components/confirm-dialog.component';
import {
  DeleteRendererParams,
  GridDeleteAction,
  GridDeleteCallback
} from '../interfaces/delete-params.interface';

@Component({
  templateUrl: './delete-button-renderer.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeleteButtonRendererComponent implements ICellRendererAngularComp {
  protected params: ICellRendererParams;
  protected title: string;
  protected message: string;
  protected gridDeleteAction: GridDeleteAction;
  protected gridDeleteCallback: GridDeleteCallback;

  public isVisible: boolean;

  constructor(
    protected dialog: MatDialog,
    protected store: Store<any>,
    protected ngZone: NgZone
  ) {}

  refresh(): boolean {
    return false;
  }

  agInit(params: ICellRendererParams & DeleteRendererParams): void {
    this.params = params;
    this.title = params.title;
    this.message = params.data && params.data[params.fieldNameForMessage];
    this.gridDeleteAction = params.gridDeleteAction;
    this.gridDeleteCallback = params.gridDeleteCallback;
    this.isVisible = params.visible ? params.visible(params) : true;
  }

  delete() {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(ConfirmDialogComponent, {
        width: '350px',
        data: {
          title: this.title,
          message: this.message,
          id: this.params.data.id,
          confirmMessage: 'Delete'
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          if (this.gridDeleteAction) {
            this.store.dispatch({
              ...this.gridDeleteAction,
              id: this.params.data.id
            });
          }

          if (this.gridDeleteCallback) {
            this.gridDeleteCallback(this.params);
          }
        }
        dialogRef = undefined;
      });
    });
  }
}
