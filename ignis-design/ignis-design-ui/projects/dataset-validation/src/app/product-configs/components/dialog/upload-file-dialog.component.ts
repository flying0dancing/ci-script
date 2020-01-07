import { HttpEvent } from '@angular/common/http';
import { Component, Inject, ViewChild } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material';
import { Observable } from 'rxjs';
import { FileItem } from '../../../core/file-upload/model/file-item';
import { FileUploadManager } from '../../../core/file-upload/service/file-upload-manager';

@Component({
  selector: 'dv-product-upload-dialog',
  templateUrl: './upload-file-dialog.component.html'
})
export class UploadFileDialogComponent {
  @ViewChild('fileInput', { static: false }) fileInput;
  uploaderManager = new FileUploadManager({
    autoUpload: true,
    fileAlias: 'file',
    uploadServiceRequest: formData => this.uploadFunction(formData)
  });
  isAttachmentUploading = false;
  title: String = '';
  private uploadFunction: (formData: FormData) => Observable<HttpEvent<number>>;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data
  ) {
    this.uploaderManager.onBeforeItemUpload = this.checkAttachmentsLoading.bind(
      this
    );
    this.uploaderManager.onCompleteItem = this.checkAttachmentsLoading.bind(
      this
    );
    this.uploaderManager.onItemUploadSuccess = this.data.onUploadSuccess.bind(
      this
    );
    this.uploadFunction = this.data.uploadFunction;
    this.title = this.data.title;
  }

  private checkAttachmentsLoading(fileItem: FileItem) {
    const queue = this.uploaderManager.queue;
    const filesUploading = queue.filter(item => item.isUploading);

    this.isAttachmentUploading = !!filesUploading.length;
  }
}
