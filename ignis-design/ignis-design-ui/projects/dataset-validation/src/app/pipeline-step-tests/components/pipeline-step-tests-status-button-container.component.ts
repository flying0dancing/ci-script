import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Store, select } from '@ngrx/store';
import { MatDialog } from '@angular/material/dialog';
import { map } from 'rxjs/operators';
import {
  PipelineStepTestsDialogContainerComponent,
  createDialogConfig
} from './pipeline-step-tests-dialog-container.component';
import * as selectors from '../reducers';
import { getOverallPipelineStepTestsStatus } from '../utilities/pipeline-step-tests.utilities';

@Component({
  selector: 'dv-pipeline-step-tests-status-button-container',
  templateUrl: './pipeline-step-tests-status-button-container.component.html',
  styleUrls: ['./pipeline-step-tests-status-button-container.component.scss']
})
export class PipelineStepTestsStatusButtonContainerComponent {
  pipelineStepTests$ = this.store.pipe(
    select(selectors.GET_PIPELINE_STEP_TESTS)
  );

  pipelineStepTestsLoading$ = this.store.pipe(
    select(selectors.GET_PIPELINE_STEP_TESTS_LOADING)
  );

  overallStatus$ = this.pipelineStepTests$.pipe(
    map(pipelineStepTests =>
      getOverallPipelineStepTestsStatus(pipelineStepTests)
    )
  );

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private route: ActivatedRoute
  ) {}

  openManager() {
    this.dialog.open(
      PipelineStepTestsDialogContainerComponent,
      createDialogConfig({ route: this.route })
    );
  }
}
