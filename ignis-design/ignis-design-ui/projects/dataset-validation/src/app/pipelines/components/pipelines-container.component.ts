import { Component, Input, NgZone, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { map } from 'rxjs/operators';
import * as pipelineActions from '../actions/pipelines.actions';
import { Pipeline } from '../interfaces/pipeline.interface';
import {
  PIPELINE_ENTITIES,
  PIPELINES_LOADING_STATE
} from '../selectors/pipelines.selector';
import { CreatePipelineDialogComponent } from './create-pipeline-dialog.component';

@Component({
  selector: 'dv-pipelines-container',
  templateUrl: './pipelines-container.component.html'
})
export class PipelinesContainerComponent implements OnInit {
  @Input() productId: number;

  pipelines$ = this.store
    .select(PIPELINE_ENTITIES)
    .pipe(
      map((pipelines: Pipeline[]) =>
        pipelines.filter(pipeline => pipeline.productId === this.productId)
      )
    );

  loading$ = this.store.select(PIPELINES_LOADING_STATE);

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.store.dispatch(new pipelineActions.GetAll());
  }

  openCreatePipelineDialog(): void {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(CreatePipelineDialogComponent, {
        width: '480px',
        maxHeight: '850px',
        disableClose: true,
        data: { productId: this.productId }
      });

      dialogRef.afterClosed().subscribe(() => {
        dialogRef = undefined;
      });
    });
  }
}
