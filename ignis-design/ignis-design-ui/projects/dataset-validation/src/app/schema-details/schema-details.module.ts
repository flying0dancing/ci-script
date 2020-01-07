import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatOptionModule } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { MonacoEditorModule } from 'ngx-monaco-editor';
import { ProductBreadcrumbComponent } from '../core/breadcrumbs/product-breadcrumb.component';

import { CoreModule } from '../core/core.module';
import { ProductConfigsModule } from '../product-configs/product-configs.module';
import { EditFieldButtonRendererComponent } from '../schemas/components/contained/edit-field-button-renderer.component';
import { EditRuleButtonRendererComponent } from '../schemas/components/contained/edit-rule-button-renderer.component';
import { FieldFormComponent } from './components/contained/field-form.component';
import { FieldOrderRendererComponent } from './components/contained/field-order-renderer.component';
import { FieldsGridComponent } from './components/contained/fields-grid.component';
import { RuleFormComponent } from './components/contained/rule-form.component';
import { RuleGridComponent } from './components/contained/rule.grid.component';
import { BooleanFieldEditorComponent } from './components/dialog/rule-examples/grid/boolean-field-editor.component';
import { ContextFieldHeaderRendererComponent } from './components/dialog/rule-examples/grid/context-field-header-renderer.component';
import { DecimalFieldEditorComponent } from './components/dialog/rule-examples/grid/decimal-field-editor.component';
import { ExampleFieldRendererComponent } from './components/dialog/rule-examples/grid/example-field-renderer.component';
import { ExpectedResultEditorComponent } from './components/dialog/rule-examples/grid/expected-result-editor.component';
import { FormattedDateFieldEditorComponent } from './components/dialog/rule-examples/grid/formatted-date-field-editor.component';
import { NumberFieldEditorComponent } from './components/dialog/rule-examples/grid/number-field-editor.component';
import { RemoveExampleRendererComponent } from './components/dialog/rule-examples/grid/remove-example-renderer.component';
import { RuleExamplesGridComponent } from './components/dialog/rule-examples/grid/rule-examples-grid.component';
import { StringFieldEditorComponent } from './components/dialog/rule-examples/grid/string-field-editor.component';
import { TestResultRendererComponent } from './components/dialog/rule-examples/grid/test-result-renderer.component';
import { RuleExamplesDialogComponent } from './components/dialog/rule-examples/rule-examples-dialog.component';
import { RuleHelpDialogComponent } from './components/dialog/rule-help-dialog.component';
import { SchemaDetailsContainerComponent } from './components/schema-details-container.component';
import { FieldFormEffects } from './effects/field-form.effect';
import { RuleFormEffects } from './effects/rule-form.effect';
import { NumbersOnlyStringPipe } from './pipes/numbers-only-string.pipe';
import { fieldFormReducer } from './reducers/field-form.reducer';
import { ruleFormReducer } from './reducers/rule-form.reducer';
import { FIELD_FORM_STATE_NAME } from './selectors/field-form.selector';
import { RULE_FORM_STATE_NAME } from './selectors/rule-form.selector';
import { FieldEventService } from './service/field-event.service';
import { RuleEventService } from './service/rule-event.service';

@NgModule({
  imports: [
    CoreModule,
    FormsModule,
    MatButtonToggleModule,
    MatDatepickerModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatCardModule,
    MatInputModule,
    MatOptionModule,
    MatRadioModule,
    MatSelectModule,
    MatCheckboxModule,
    MonacoEditorModule,
    ProductConfigsModule,
    StoreModule.forFeature(RULE_FORM_STATE_NAME, ruleFormReducer),
    StoreModule.forFeature(FIELD_FORM_STATE_NAME, fieldFormReducer),
    EffectsModule.forFeature([RuleFormEffects]),
    EffectsModule.forFeature([FieldFormEffects])
  ],
  declarations: [
    ContextFieldHeaderRendererComponent,
    EditFieldButtonRendererComponent,
    EditRuleButtonRendererComponent,
    BooleanFieldEditorComponent,
    ExampleFieldRendererComponent,
    DecimalFieldEditorComponent,
    FormattedDateFieldEditorComponent,
    NumberFieldEditorComponent,
    ExpectedResultEditorComponent,
    StringFieldEditorComponent,
    FieldsGridComponent,
    NumbersOnlyStringPipe,
    RemoveExampleRendererComponent,
    RuleFormComponent,
    FieldFormComponent,
    RuleGridComponent,
    RuleHelpDialogComponent,
    RuleExamplesDialogComponent,
    RuleExamplesGridComponent,
    SchemaDetailsContainerComponent,
    TestResultRendererComponent,
    FieldOrderRendererComponent
  ],
  entryComponents: [
    ContextFieldHeaderRendererComponent,
    EditFieldButtonRendererComponent,
    EditRuleButtonRendererComponent,
    BooleanFieldEditorComponent,
    ExampleFieldRendererComponent,
    DecimalFieldEditorComponent,
    FormattedDateFieldEditorComponent,
    NumberFieldEditorComponent,
    StringFieldEditorComponent,
    ExpectedResultEditorComponent,
    RemoveExampleRendererComponent,
    RuleHelpDialogComponent,
    RuleExamplesDialogComponent,
    TestResultRendererComponent,
    ProductBreadcrumbComponent,
    FieldOrderRendererComponent
  ],
  providers: [RuleEventService, FieldEventService],
  exports: []
})
export class SchemaDetailsModule {}
