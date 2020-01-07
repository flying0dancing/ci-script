import { HttpEvent, HttpEventType, HttpResponse } from "@angular/common/http";
import { Observable } from "rxjs";

import { FileItem } from "./file-item";

interface ManagerOptions {
  autoUpload?: boolean;
  fileAlias: string;
  fileSizeLimit?: number;
  uploadServiceRequest: (formData: FormData) => Observable<HttpEvent<any>>;
}

function isFile(f: any): boolean {
  // https://hs2n.wordpress.com/2012/08/13/detecting-folders-in-html-drop-area/
  return (
    File &&
    f instanceof File &&
    !(!f.type && f.size % 4096 === 0 && f.size <= 102400)
  );
}

export class FileUploadManager {
  queue: FileItem[] = [];

  private validators = [{ name: "file", fn: isFile }];

  private options: ManagerOptions = {
    autoUpload: false,
    fileAlias: undefined,
    fileSizeLimit: undefined,
    uploadServiceRequest: undefined
  };

  constructor(options: ManagerOptions) {
    this.configure(options);
  }

  configure(options: ManagerOptions) {
    this.options = { ...this.options, ...options };

    if (options.fileSizeLimit) {
      this.validators.push({ name: "fileSize", fn: this.isValidFileSize });
    }
  }

  isValidFileSize(file: File): boolean {
    return (
      typeof this.options.fileSizeLimit === "number" &&
      file.size < this.options.fileSizeLimit
    );
  }

  getValidatorIndex(file): number {
    let index = -1;

    for (let i = 0; i < this.validators.length; i++) {
      if (!this.validators[i].fn(file)) {
        index = i;
        break;
      }
    }

    return index;
  }

  addToQueue(files: FileList) {
    this.forEachFile(files, file => {
      const index = this.getValidatorIndex(file);

      if (index === -1) {
        const fileItem = new FileItem(file, this);

        this.queue.push(fileItem);

        if (this.options.autoUpload) {
          this.upload(fileItem);
        }
      } else {
        this._onAddFileFailed(file, this.validators[index]);
      }
    });
  }

  removeFromQueue(fileItem: FileItem) {
    const index = this.getQueueFileItemIndex(fileItem);
    const item = this.getFileFromQueue(fileItem);

    if (item) {
      const isUploading = { ...item }.isUploading;

      if (isUploading) {
        item.cancel();
      }

      this.queue.splice(index, 1);

      if (isUploading) {
        this.onCompleteItem(fileItem, undefined, undefined);
      }

      this.onItemRemovedFromQueue(item);
    }
  }

  clearQueue() {
    while (this.queue.length) {
      this.queue[0].remove();
    }
  }

  cancelItem(fileItem: FileItem) {
    const item = this.getFileFromQueue(fileItem);

    if (item && item.request && item.isUploading) {
      item.request.unsubscribe();
    }

    this._onItemUploadCancel(item);
  }

  upload(fileItem: FileItem) {
    this._onBeforeItemUpload(fileItem);

    const formData = new FormData();
    formData.append(
      this.options.fileAlias,
      fileItem.file.rawFile,
      fileItem.file.name
    );

    fileItem.request = this.options.uploadServiceRequest(formData).subscribe(
      event => {
        if (event.type === HttpEventType.UploadProgress) {
          const progress = Math.round((100 * event.loaded) / event.total);

          this._onProgressItem(fileItem, progress);
        } else if (event instanceof HttpResponse) {
          this._onItemUploadSuccess(fileItem, event.body);
          this.onCompleteItem(fileItem, event.body, event);
        }
      },
      err => {
        this._onItemUploadFail(fileItem, event);
        this.onCompleteItem(fileItem, undefined, err);
      }
    );
  }

  forEachFile(files: FileList, callback: (file: File) => any) {
    for (let i = 0; i < files.length; i++) {
      callback(files[i]);
    }
  }

  onBeforeItemUpload(fileItem: FileItem) {}

  onProgressItem(fileItem: FileItem, progress) {}

  onItemUploadSuccess(fileItem: FileItem, response) {}

  onItemUploadFail(fileItem: FileItem, event: any) {}

  onCompleteItem(fileItem: FileItem, response: any, event: any) {}

  onItemUploadCancel(fileItem: FileItem) {}

  onItemRemovedFromQueue(fileItem: FileItem) {}

  onAddFileFailed(
    file: File,
    validator: { name: string; fn: (file: File) => boolean }
  ) {}

  private getQueueFileItemIndex(fileItem: FileItem) {
    return this.queue.indexOf(fileItem);
  }

  private getFileFromQueue(fileItem: FileItem): FileItem {
    const index = this.getQueueFileItemIndex(fileItem);

    return index !== -1 ? fileItem : undefined;
  }

  private _onBeforeItemUpload(fileItem: FileItem) {
    fileItem.onBeforeUpload();
    this.onBeforeItemUpload(fileItem);
  }

  private _onProgressItem(fileItem: FileItem, progress) {
    fileItem.onProgress(progress);
    this.onProgressItem(fileItem, progress);
  }

  private _onItemUploadSuccess(fileItem: FileItem, response) {
    fileItem.onSuccess(response);
    this.onItemUploadSuccess(fileItem, response);
  }

  private _onItemUploadFail(fileItem: FileItem, event: any) {
    fileItem.onFail(event);
    this.onItemUploadFail(fileItem, event);
  }

  private _onItemUploadCancel(fileItem: FileItem) {
    fileItem.onCancel();
    this.onItemUploadCancel(fileItem);
  }

  private _onAddFileFailed(
    file: File,
    validator: { name: string; fn: (file: File) => boolean }
  ) {
    this.onAddFileFailed(file, validator);
  }
}
