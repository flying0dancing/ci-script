import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { AgGridModule } from 'ag-grid-angular';
import { HotkeyModule } from 'angular2-hotkeys';
import { ProductBreadcrumbComponent } from './breadcrumbs/product-breadcrumb.component';
import { DagModule } from './dag/dag.module';
import { ConfirmDialogComponent } from './dialogs/components/confirm-dialog.component';
import { ErrorDialogComponent } from './dialogs/components/error-dialog.component';
import { FeedbackDialogComponent } from './dialogs/components/feedback-dialog.component';
import { FeedbackDialogService } from './dialogs/components/feedback-dialog.service';
import { InfoDialogComponent } from './dialogs/components/info-dialog.component';
import { FileUploadModule } from './file-upload/file-upload.module';
import { AddItemButtonComponent } from './forms/components/add-item-button.component';
import { AutoCompleteFormComponent } from './forms/components/auto-complete-form.component';
import { ChipListInputComponent } from './forms/components/chip-list-input.component';
import { ExportItemButtonComponent } from './forms/components/export-item-button.component';
import { ImportItemButtonComponent } from './forms/components/import-item-button.component';
import { InlineDateEditInputComponent } from './forms/components/inline-date-edit-input.component';
import { InlineEditComponent } from './forms/components/inline-edit-input.component';
import { InputLabelComponent } from './forms/components/input-label.component';
import { SaveButtonComponent } from './forms/components/save-button.component';
import { DateFormatPipe } from './grid/components/date-format-pipe.component';
import { DateRendererComponent } from './grid/components/date-renderer.component';
import { DeleteButtonRendererComponent } from './grid/components/delete-button-renderer.component';
import { DeleteSchemaButtonRendererComponent } from './grid/components/delete-schema-button-renderer.component';
import { HttpErrorModule } from './interceptors/http-error/http-error.module';
import { ContainerComponent } from './layout/components/container.component';
import { HeaderComponent } from './layout/components/header.component';
import { SidenavComponent } from './layout/components/sidenav.component';

@NgModule({
  imports: [
    AgGridModule,
    CommonModule,
    HttpErrorModule,
    HotkeyModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    MatSidenavModule,
    MatChipsModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    RouterModule,
    MatInputModule,
    MatSelectModule,
    FormsModule,
    FileUploadModule,
    DagModule
  ],
  declarations: [
    InfoDialogComponent,
    ConfirmDialogComponent,
    FeedbackDialogComponent,
    ContainerComponent,
    DateRendererComponent,
    DeleteButtonRendererComponent,
    DeleteSchemaButtonRendererComponent,
    ErrorDialogComponent,
    HeaderComponent,
    AddItemButtonComponent,
    SaveButtonComponent,
    SidenavComponent,
    InlineEditComponent,
    InputLabelComponent,
    InlineDateEditInputComponent,
    ProductBreadcrumbComponent,
    ChipListInputComponent,
    DateFormatPipe,
    AutoCompleteFormComponent,
    ImportItemButtonComponent,
    ExportItemButtonComponent
  ],
  entryComponents: [
    InfoDialogComponent,
    ConfirmDialogComponent,
    FeedbackDialogComponent,
    ContainerComponent,
    DateRendererComponent,
    DeleteButtonRendererComponent,
    DeleteSchemaButtonRendererComponent,
    ErrorDialogComponent,
    HeaderComponent,
    SidenavComponent,
    InlineEditComponent,
    InlineDateEditInputComponent,
    ProductBreadcrumbComponent,
    ChipListInputComponent
  ],
  providers: [FeedbackDialogService],
  exports: [
    ContainerComponent,
    HeaderComponent,
    AddItemButtonComponent,
    ImportItemButtonComponent,
    ExportItemButtonComponent,
    SaveButtonComponent,
    SidenavComponent,
    InlineEditComponent,
    InputLabelComponent,
    InlineDateEditInputComponent,
    ProductBreadcrumbComponent,
    ChipListInputComponent,
    AutoCompleteFormComponent,
    AgGridModule,
    CommonModule,
    HotkeyModule,
    MatButtonModule,
    MatCardModule,
    MatDialogModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    MatSidenavModule,
    MatInputModule,
    MatSelectModule,
    MatChipsModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    RouterModule,
    HttpClientModule,
  ]
})
export class CoreModule {}
