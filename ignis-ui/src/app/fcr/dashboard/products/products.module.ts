import * as API from "@/core/api/products/products.module";
import { GroupedProductsGridComponent } from "@/fcr/dashboard/products/grouped/grouped-products-grid.component";
import { ProductUploadDialogComponent } from "@/fcr/dashboard/products/product-upload-dialog.component";
import { ProductsComponent } from "@/fcr/dashboard/products/products.component";
import { ImportStatusRendererComponent } from "@/fcr/dashboard/products/renderers/import-status.renderer";
import { UnstagedProductsComponent } from "@/fcr/dashboard/products/unstaged/unstaged-products.component";
import { ViewRuleButtonRendererComponent } from "@/fcr/dashboard/products/view-rule-button-renderer.component";
import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatMenuModule } from "@angular/material/menu";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { FileUploadModule } from "../shared/file-upload/file-upload.module";
import { GridModule } from "../shared/grid/grid.module";

@NgModule({
  imports: [
    CommonModule,
    GridModule,
    FileUploadModule,
    FormsModule,
    ReactiveFormsModule,
    LayoutModule,
    LoadersModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatSnackBarModule,
    API.ProductsModule
  ],
  exports: [
    ProductsComponent,
    ProductUploadDialogComponent,
    UnstagedProductsComponent
  ],
  declarations: [
    ImportStatusRendererComponent,
    GroupedProductsGridComponent,
    ProductsComponent,
    ProductUploadDialogComponent,
    UnstagedProductsComponent,
    ViewRuleButtonRendererComponent
  ],
  entryComponents: [
    ImportStatusRendererComponent,
    GroupedProductsGridComponent,
    ProductsComponent,
    ProductUploadDialogComponent,
    UnstagedProductsComponent,
    ViewRuleButtonRendererComponent
  ]
})
export class ProductsModule {}
