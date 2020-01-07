import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { FileDropComponent } from './components/file-drop.component';
import { FileDropDirective } from './service/file-drop.directive';
import { FileSelectDirective } from './service/file-select.directive';
import { FileUploadsItemComponent } from './components/file-uploads-item.component';
import { FileUploadsComponent } from './components/file-uploads.component';

@NgModule({
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    MatTooltipModule
  ],
  declarations: [
    FileDropComponent,
    FileDropDirective,
    FileSelectDirective,
    FileUploadsComponent,
    FileUploadsItemComponent
  ],
  exports: [
    FileDropComponent,
    FileDropDirective,
    FileSelectDirective,
    FileUploadsComponent,
    FileUploadsItemComponent
  ]
})
export class FileUploadModule {}
