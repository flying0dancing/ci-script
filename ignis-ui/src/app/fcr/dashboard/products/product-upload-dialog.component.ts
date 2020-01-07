import * as PipelineActions from '@/core/api/pipelines/pipelines.actions';
import { NAMESPACE as PIPELINE_NAMESPACE } from '@/core/api/pipelines/pipelines.reducer';
import * as ProductActions from "@/core/api/products/products.actions";
import { NAMESPACE } from "@/core/api/products/products.reducer";
import { ProductsService } from "@/core/api/products/products.service";
import { Component, ViewChild } from "@angular/core";
import { Store } from "@ngrx/store";
import { FileItem } from "../shared/file-upload/file-item";
import { FileUploadManager } from "../shared/file-upload/file-upload-manager";

@Component({
  selector: "app-product-upload-dialog",
  templateUrl: "./product-upload-dialog.component.html"
})
export class ProductUploadDialogComponent {
  @ViewChild("fileInput", { static: false }) fileInput;
  uploaderManager = new FileUploadManager({
    autoUpload: true,
    fileAlias: "file",
    uploadServiceRequest: formData => this.productsService.upload(formData)
  });
  isAttachmentUploading = false;

  constructor(
    private productsService: ProductsService,
    private store: Store<any>
  ) {
    this.uploaderManager.onBeforeItemUpload = this.checkAttachmentsLoading.bind(
      this
    );
    this.uploaderManager.onCompleteItem = this.checkAttachmentsLoading.bind(
      this
    );
    this.uploaderManager.onItemUploadSuccess = this.handleItemUploadSuccess.bind(
      this
    );
  }

  private checkAttachmentsLoading(fileItem: FileItem) {
    const queue = this.uploaderManager.queue;
    const filesUploading = queue.filter(item => item.isUploading);

    this.isAttachmentUploading = !!filesUploading.length;
  }

  private handleItemUploadSuccess() {
    this.store.dispatch(
      new ProductActions.ImportStarted({ reducerMapKey: NAMESPACE })
    );
    this.store.dispatch(new ProductActions.Get({ reducerMapKey: NAMESPACE }));
    this.store.dispatch(
      new PipelineActions.Get({ reducerMapKey: PIPELINE_NAMESPACE })
    );
  }
}
