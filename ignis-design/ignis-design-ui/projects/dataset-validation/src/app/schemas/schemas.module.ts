import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';

import { CoreModule } from '../core/core.module';
import { CopySchemaButtonComponent } from './components/contained/copy-schema-button.component';
import { CreateNewVersionButtonComponent } from './components/contained/create-new-version-button.component';
import { CreateSchemaDialogComponent } from './components/contained/create-schema-dialog.component';
import { CreateNewVersionDialogComponent } from './components/contained/dialog/create-new-version-dialog.component';
import { EditSchemaButtonRendererComponent } from './components/contained/edit-schema-button-renderer.component';
import { SchemaGridComponent } from './components/contained/schema-grid.component';
import { SchemasContainerComponent } from './components/schemas-container.component';
import { SchemasEffects } from './effects/schemas.effects';
import {
  REDUCERS,
  SCHEMAS_STATE_NAME
} from './reducers/schemas-state.selector';
import { SchemaGridEventService } from './services/schema-grid.service';

@NgModule({
  imports: [
    CoreModule,
    MatCardModule,
    MatCheckboxModule,
    MatDatepickerModule,
    MatExpansionModule,
    MatInputModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    StoreModule.forFeature(SCHEMAS_STATE_NAME, REDUCERS),
    EffectsModule.forFeature([SchemasEffects])
  ],
  declarations: [
    CreateSchemaDialogComponent,
    CreateNewVersionDialogComponent,
    EditSchemaButtonRendererComponent,
    SchemaGridComponent,
    SchemasContainerComponent,
    CreateNewVersionButtonComponent,
    CopySchemaButtonComponent
  ],
  entryComponents: [
    CreateSchemaDialogComponent,
    CreateNewVersionDialogComponent,
    EditSchemaButtonRendererComponent,
    CreateNewVersionButtonComponent,
    CopySchemaButtonComponent
  ],
  exports: [SchemaGridComponent, SchemasContainerComponent],
  providers: [SchemaGridEventService]
})
export class SchemasModule {}
