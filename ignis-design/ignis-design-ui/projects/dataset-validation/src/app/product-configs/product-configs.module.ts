import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogModule } from '@angular/material';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTreeModule } from '@angular/material/tree';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';

import { CoreModule } from '../core/core.module';
import { FileUploadModule } from '../core/file-upload/file-upload.module';
import { PipelinesModule } from '../pipelines/pipelines.module';
import { SchemasModule } from '../schemas/schemas.module';
import { ConfirmDownloadDialogComponent } from './components/contained/confirm-download-dialog.component';
import { CreateProductConfigDialogComponent } from './components/contained/create-product-config-dialog.component';
import { ExportProductConfigRendererComponent } from './components/contained/export-product-config-renderer.component';
import { OpenProductConfigRendererComponent } from './components/contained/open-product-config-renderer.component';
import { ProductConfigsGridComponent } from './components/contained/product-configs-grid.component';
import { ValidateProductDialogComponent } from './components/contained/validate-product-dialog.component';
import { UploadFileDialogComponent } from './components/dialog/upload-file-dialog.component';
import { ProductConfigContainerComponent } from './components/product-config-container.component';
import { ProductConfigsContainerComponent } from './components/product-configs-container.component';
import { ProductConfigsEffects } from './effects/product-configs.effects';
import { REDUCERS } from './reducers';

@NgModule({
  imports: [
    CoreModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatInputModule,
    MatIconModule,
    MatProgressSpinnerModule,
    SchemasModule,
    PipelinesModule,
    FileUploadModule,
    StoreModule.forFeature('product-configs', REDUCERS),
    EffectsModule.forFeature([ProductConfigsEffects]),
    MatTreeModule,
    MatCheckboxModule,
    MatDialogModule
  ],
  declarations: [
    CreateProductConfigDialogComponent,
    ValidateProductDialogComponent,
    ExportProductConfigRendererComponent,
    OpenProductConfigRendererComponent,
    ProductConfigsContainerComponent,
    ProductConfigContainerComponent,
    ProductConfigsGridComponent,
    UploadFileDialogComponent,
    ConfirmDownloadDialogComponent
  ],
  entryComponents: [
    CreateProductConfigDialogComponent,
    ValidateProductDialogComponent,
    ExportProductConfigRendererComponent,
    OpenProductConfigRendererComponent,
    UploadFileDialogComponent,
    ConfirmDownloadDialogComponent
  ],
  exports: []
})
export class ProductConfigsModule {}
