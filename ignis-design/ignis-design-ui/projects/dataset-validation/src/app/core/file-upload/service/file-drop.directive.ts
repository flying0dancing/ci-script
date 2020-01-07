import { Directive, HostListener, Input } from '@angular/core';

import { FileUploadManager } from './file-upload-manager';

@Directive({
  selector: '[dvFileDrop]'
})
export class FileDropDirective {
  @Input() uploadManager: FileUploadManager;

  @HostListener('drop', ['$event'])
  public onDrop(e: DragEvent) {
    this.preventAndStop(e);

    this.uploadManager.addToQueue(e.dataTransfer.files);
  }

  @HostListener('dragover', ['$event'])
  public onDragOver(e: DragEvent) {
    this.preventAndStop(e);
  }

  @HostListener('dragleave', ['$event'])
  public onDragLeave(e: DragEvent): any {
    this.preventAndStop(e);
  }

  private preventAndStop(e: DragEvent) {
    e.preventDefault();
    e.stopPropagation();
  }
}
