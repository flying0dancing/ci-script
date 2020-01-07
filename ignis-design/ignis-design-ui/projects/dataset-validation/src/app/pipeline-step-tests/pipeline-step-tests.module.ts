import { NgModule } from '@angular/core';
import { MatDatepickerModule } from '@angular/material';
import { MatMenuModule } from '@angular/material/menu';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { CoreModule } from '../core/core.module';
import { EditPipelineStepTestButtonRendererComponent } from './components/edit-pipeline-step-test-button-renderer.component';
import { PipelineStepTestEditorComponent } from './components/pipeline-step-test-editor.component';
import { PipelineStepTestFormComponent } from './components/pipeline-step-test-form.component';
import { PipelineStepTestInputDataGridComponent } from './components/pipeline-step-test-input-data-grid.component';
import { PipelineStepTestOutputDataGridComponent } from './components/pipeline-step-test-output-data-grid.component';
import { PipelineStepTestOutputDataRowStatusIconComponent } from './components/pipeline-step-test-output-data-row-status-icon.component';
// tslint:disable-next-line: max-line-length
import { PipelineStepTestOutputDataRowStatusRendererComponent } from './components/pipeline-step-test-output-data-row-status-renderer.component';
import { PipelineStepTestPageContainerComponent } from './components/pipeline-step-test-page-container.component';
import { PipelineStepTestStatusIconComponent } from './components/pipeline-step-test-status-icon.component';
import { PipelineStepTestStatusRendererComponent } from './components/pipeline-step-test-status-renderer.component';
import { PipelineStepTestsDialogContainerComponent } from './components/pipeline-step-tests-dialog-container.component';
import { PipelineStepTestsGridComponent } from './components/pipeline-step-tests-grid.component';
import { PipelineStepTestsStatusButtonContainerComponent } from './components/pipeline-step-tests-status-button-container.component';
import { PipelineStepTestRowsEffects } from './effects/pipeline-step-test-rows.effects';
import { PipelineStepTestEffects } from './effects/pipeline-step-test.effects';
import { PipelineStepTestsEffects } from './effects/pipeline-step-tests.effects';
import { REDUCERS, STATE_KEY } from './reducers';

@NgModule({
  imports: [
    CoreModule,
    StoreModule.forFeature(STATE_KEY, REDUCERS),
    EffectsModule.forFeature([
      PipelineStepTestsEffects,
      PipelineStepTestEffects,
      PipelineStepTestRowsEffects
    ]),
    MatDatepickerModule,
    MatMenuModule
  ],
  declarations: [
    EditPipelineStepTestButtonRendererComponent,
    PipelineStepTestsGridComponent,
    PipelineStepTestsStatusButtonContainerComponent,
    PipelineStepTestStatusIconComponent,
    PipelineStepTestsDialogContainerComponent,
    EditPipelineStepTestButtonRendererComponent,
    PipelineStepTestStatusRendererComponent,
    PipelineStepTestPageContainerComponent,
    PipelineStepTestFormComponent,
    PipelineStepTestEditorComponent,
    PipelineStepTestInputDataGridComponent,
    PipelineStepTestOutputDataGridComponent,
    PipelineStepTestOutputDataRowStatusIconComponent,
    PipelineStepTestOutputDataRowStatusRendererComponent
  ],
  exports: [PipelineStepTestsStatusButtonContainerComponent],
  entryComponents: [
    PipelineStepTestsDialogContainerComponent,
    EditPipelineStepTestButtonRendererComponent,
    PipelineStepTestStatusRendererComponent,
    PipelineStepTestOutputDataRowStatusRendererComponent
  ]
})
export class PipelineStepTestsModule {}
