import { Subscription } from "rxjs";

import { FileLikeObject } from "./file-like-object";
import { FileUploadManager } from "./file-upload-manager";

export class FileItem {
  file: FileLikeObject;

  uploadManager: FileUploadManager;

  isCancel = false;

  isFail = false;

  isSuccess = false;

  isUploading = false;

  progress = 0;

  request: Subscription;

  constructor(rawFile: File, uploadManager: FileUploadManager) {
    this.file = new FileLikeObject(rawFile);
    this.uploadManager = uploadManager;
  }

  cancel() {
    this.uploadManager.cancelItem(this);
  }

  remove() {
    this.uploadManager.removeFromQueue(this);
  }

  upload() {
    this.uploadManager.upload(this);
  }

  onBeforeUpload() {
    this.isCancel = false;
    this.isFail = false;
    this.isSuccess = false;
    this.isUploading = true;
    this.progress = 0;
  }

  onProgress(progress) {
    this.progress = progress;
  }

  onSuccess(response: any) {
    this.isCancel = false;
    this.isFail = false;
    this.isSuccess = true;
    this.isUploading = false;
    this.progress = 100;
  }

  onFail(event: any) {
    this.isCancel = false;
    this.isFail = true;
    this.isSuccess = false;
    this.isUploading = false;
    this.progress = 0;
  }

  onCancel() {
    this.isCancel = true;
    this.isFail = false;
    this.isSuccess = false;
    this.isUploading = false;
    this.progress = 0;
  }
}
