import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { MatPaginatorModule } from '@angular/material';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatOptionModule } from '@angular/material/core';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatSliderModule } from '@angular/material/slider';
import { MatTabsModule } from '@angular/material/tabs';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { AngularDraggableModule } from 'angular2-draggable';
import { MonacoEditorModule } from 'ngx-monaco-editor';
import { REDUCERS } from 'projects/dataset-validation/src/app/pipelines/reducers';
import { CoreModule } from '../core/core.module';
import { DagModule } from '../core/dag/dag.module';
import { PipelineStepTestsModule } from '../pipeline-step-tests/pipeline-step-tests.module';
import { CreatePipelineDialogComponent } from './components/create-pipeline-dialog.component';
import { SelectEditorDialogComponent } from './components/dialog/select-editor-dialog.component';
import { EditPipelineButtonRendererComponent } from './components/edit-pipeline-button-renderer.component';
import { EditPipelineStepButtonRendererComponent } from './components/edit-pipeline-step-button-renderer.component';
import { PipelineDetailsContainerComponent } from './components/pipeline-details-container.component';
import { PipelineSelectEditorComponent } from './components/pipeline-select-editor.component';
import { PipelineStepsGridComponent } from './components/pipeline-steps-grid.component';
import { PipelineValidationRendererComponent } from './components/pipeline-validation-renderer-component';
import { PipelinesContainerComponent } from './components/pipelines-container.component';
import { PipelinesGridComponent } from './components/pipelines-grid.component';
import { PipelineAggregationStepSubFormComponent } from './components/step-form/aggregation/pipeline-aggregation-step-sub-form.component';
import { PipelineGroupingsFormComponent } from './components/step-form/aggregation/pipeline-groupings-form.component';
import { PipelineStepFormErrorsComponent } from './components/step-form/errors/pipeline-step-form-errors.component';
import { PipelineStepFormSingleErrorComponent } from './components/step-form/errors/pipeline-step-form-single-error.component';
import { PipelineFiltersFormComponent } from './components/step-form/filters/pipeline-filters-form.component';
import { PipelineJoinFieldFormComponent } from './components/step-form/join/pipeline-join-field-form.component';
import { PipelineJoinFieldsFormComponent } from './components/step-form/join/pipeline-join-fields-form.component';
import { PipelineJoinStepSubFormComponent } from './components/step-form/join/pipeline-join-step-sub-form.component';
import { PipelineJoinsFormComponent } from './components/step-form/join/pipeline-joins-form.component';
import { PipelineSingleJoinFormComponent } from './components/step-form/join/pipeline-single-join-form.component';
import { PipelineMapStepSubFormComponent } from './components/step-form/map/pipeline-map-step-sub-form.component';
import { PipelineStepRootFormComponent } from './components/step-form/pipeline-step-root-form.component';
import { PipelineScriptletStepSubFormComponent } from './components/step-form/Scriptlet/pipeline-scriptlet-step-sub-form.component';
import { PipelineSelectsFormComponent } from './components/step-form/select/pipeline-selects-form.component';
import { PipelineSingleSelectFormComponent } from './components/step-form/select/pipeline-single-select-form.component';
import { PipelineUnionFormComponent } from './components/step-form/union/pipeline-union-form.component';
import { PipelineUnionSchemasFormComponent } from './components/step-form/union/pipeline-union-schemas-form.component';
import { PipelineUnionsStepSubFormComponent } from './components/step-form/union/pipeline-unions-step-sub-form.component';
import { PipelineOrderByFormComponent } from './components/step-form/window/pipeline-order-by-form.component';
import { PipelinePartitionsFormComponent } from './components/step-form/window/pipeline-partitions-form.component';
import { PipelineSingleWindowSelectFormComponent } from './components/step-form/window/pipeline-single-window-select-form.component';
import { PipelineWindowStepSubFormComponent } from './components/step-form/window/pipeline-window-step-sub-form.component';
import { PipelineStepFormEffects } from './effects/pipeline-step-form.effect';
import { PipelinesEffects } from './effects/pipelines.effects';
import { pipelineStepFormReducer } from './reducers/pipeline-step-form.reducer';
import { PIPELINE_STEP_FORM_STATE_NAME } from './selectors/pipeline-step-form.selector';
import { PipelineStepEventService } from './services/pipeline-step-event.service';

@NgModule({
  imports: [
    CoreModule,
    MatCardModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatOptionModule,
    MatSelectModule,
    MatCheckboxModule,
    MatChipsModule,
    MatAutocompleteModule,
    MatSliderModule,
    MatTabsModule,
    DagModule,
    MonacoEditorModule,
    MatPaginatorModule,
    StoreModule.forFeature(
      PIPELINE_STEP_FORM_STATE_NAME,
      pipelineStepFormReducer
    ),
    StoreModule.forFeature('pipelines', REDUCERS),
    EffectsModule.forFeature([PipelinesEffects, PipelineStepFormEffects]),
    MatSlideToggleModule,
    PipelineStepTestsModule,
    AngularDraggableModule,
    DragDropModule
  ],
  declarations: [
    PipelinesContainerComponent,
    CreatePipelineDialogComponent,
    SelectEditorDialogComponent,
    PipelinesGridComponent,
    PipelineStepsGridComponent,
    PipelineSelectEditorComponent,
    EditPipelineButtonRendererComponent,
    EditPipelineStepButtonRendererComponent,
    PipelineValidationRendererComponent,
    PipelineDetailsContainerComponent,
    PipelineStepRootFormComponent,
    PipelineMapStepSubFormComponent,
    PipelineSingleSelectFormComponent,
    PipelineSelectsFormComponent,
    PipelineFiltersFormComponent,
    PipelineAggregationStepSubFormComponent,
    PipelineGroupingsFormComponent,
    PipelineSelectsFormComponent,
    PipelineJoinStepSubFormComponent,
    PipelineSingleJoinFormComponent,
    PipelineJoinsFormComponent,
    PipelineStepFormErrorsComponent,
    PipelineSingleWindowSelectFormComponent,
    PipelinePartitionsFormComponent,
    PipelineWindowStepSubFormComponent,
    PipelineOrderByFormComponent,
    PipelineUnionsStepSubFormComponent,
    PipelineScriptletStepSubFormComponent,
    PipelineUnionSchemasFormComponent,
    PipelineOrderByFormComponent,
    PipelineJoinFieldsFormComponent,
    PipelineJoinFieldFormComponent,
    PipelineUnionFormComponent,
    PipelineStepFormSingleErrorComponent
  ],
  entryComponents: [
    CreatePipelineDialogComponent,
    SelectEditorDialogComponent,
    EditPipelineButtonRendererComponent,
    EditPipelineStepButtonRendererComponent,
    PipelineValidationRendererComponent
  ],
  exports: [PipelinesContainerComponent],
  providers: [PipelineStepEventService]
})
export class PipelinesModule {}
