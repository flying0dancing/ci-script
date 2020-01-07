import {
  Component,
  ChangeDetectionStrategy,
  Inject,
  OnDestroy
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  MAT_DIALOG_DATA,
  MatDialogConfig,
  MatDialogRef
} from '@angular/material/dialog';
import { select, Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { map, switchMap, withLatestFrom } from 'rxjs/operators';
import { RowDeleteEvent } from './pipeline-step-tests-grid.component';
import * as selectors from '../reducers';
import { PipelineStepTestEventService } from '../services/pipeline-step-test-event.service';
import { formatEditPipelineStepTestUrl } from '../utilities/pipeline-step-tests.utilities';
import { Delete } from '../actions/pipeline-step-test.actions';
import { getPipeline } from '../../pipelines/selectors/pipelines.selector';
import { Pipeline } from '../../pipelines/interfaces/pipeline.interface';

interface DialogData {
  route: ActivatedRoute;
}

export function createDialogConfig(
  data: DialogData
): MatDialogConfig<DialogData> {
  return {
    width: '1000px',
    data
  };
}

@Component({
  selector: 'dv-pipeline-step-tests-dialog-container',
  templateUrl: './pipeline-step-tests-dialog-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestsDialogContainerComponent implements OnDestroy {
  pipelineIdParam$: Observable<number> = this.data.route.paramMap.pipe(
    map(params => parseInt(params.get('id'), 10))
  );

  productIdParam$: Observable<number> = this.data.route.paramMap.pipe(
    map(params => parseInt(params.get('productId'), 10))
  );

  pipeline$: Observable<Pipeline> = this.pipelineIdParam$.pipe(
    switchMap(id => this.store.select(getPipeline(id)))
  );

  pipelineStepTests$ = this.store.pipe(
    select(selectors.GET_PIPELINE_STEP_TESTS)
  );

  pipelineStepTestsLoading$ = this.store.pipe(
    select(selectors.GET_PIPELINE_STEP_TESTS_LOADING)
  );

  pipelineStepTestEditEventSubscription: Subscription = this.pipelineStepTestEventService.editEvent
    .pipe(
      withLatestFrom(this.productIdParam$),
      map(([editEvent, productId]) => ({
        productId,
        pipelineId: editEvent.pipelineStepTest.pipelineId,
        pipelineStepTestId: editEvent.pipelineStepTest.id
      }))
    )
    .subscribe(data => {
      this.dialogRef.close();
      this.router.navigate([formatEditPipelineStepTestUrl(data)]);
    });

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: DialogData,
    public dialogRef: MatDialogRef<PipelineStepTestsDialogContainerComponent>,
    private store: Store<any>,
    private pipelineStepTestEventService: PipelineStepTestEventService,
    private router: Router
  ) {}

  ngOnDestroy() {
    this.pipelineStepTestEditEventSubscription.unsubscribe();
  }

  handleRowDelete(event: RowDeleteEvent) {
    this.store.dispatch(new Delete(event.id));
  }
}
