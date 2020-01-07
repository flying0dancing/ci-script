import { Directive, ElementRef, HostListener, Input } from '@angular/core';

import { FileUploadManager } from './file-upload-manager';

@Directive({
  selector: '[dvFileSelect]'
})
export class FileSelectDirective {
  @Input() uploadManager: FileUploadManager;

  constructor(private element: ElementRef) {}

  @HostListener('change')
  public onChange(): any {
    const files = this.element.nativeElement.files;

    this.uploadManager.addToQueue(files);

    this.element.nativeElement.value = '';
  }
}
