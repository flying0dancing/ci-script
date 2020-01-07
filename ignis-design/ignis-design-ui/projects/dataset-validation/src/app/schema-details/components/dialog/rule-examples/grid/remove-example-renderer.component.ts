import { Component, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AgRendererComponent } from 'ag-grid-angular';
import { ConfirmDialogComponent } from '../../../../../core/dialogs/components/confirm-dialog.component';

@Component({
  selector: 'dv-remove-example',
  templateUrl: './remove-example-renderer.component.html'
})
export class RemoveExampleRendererComponent implements AgRendererComponent {
  removeMsg: string;

  private deleteExampleEvent: EventEmitter<any>;
  private nodeId: number;

  constructor(private dialog: MatDialog) {}

  agInit(params: any): void {
    this.deleteExampleEvent = params.deleteExampleEvent;
    this.nodeId = Number.parseInt(params.node.id, 10);
    this.removeMsg = `Remove example no. ${this.nodeId + 1}`;
  }

  removeTest() {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '385px',
      data: {
        title: this.removeMsg.toUpperCase(),
        message: `Are you sure you want to permanently remove this example?`,
        confirmMessage: this.removeMsg
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.deleteExampleEvent.emit(this.nodeId);
      }
      dialogRef = undefined;
    });
  }

  refresh(params: any): boolean {
    return false;
  }
}
