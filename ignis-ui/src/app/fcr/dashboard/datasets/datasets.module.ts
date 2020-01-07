import { DatasetsModule } from "@/core/api/datasets/datasets.module";
import { FeaturesModule } from "@/core/api/features/features.module";
import { DatasetRowMenuRendererComponent } from "@/fcr/dashboard/datasets/dataset-row-menu-renderer.component";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { MatTooltipModule } from "@angular/material/tooltip";
import { GridModule } from "../shared/grid/grid.module";
import { DatasetsComponent } from "./datasets.component";
import { DatasetsListComponent } from "./datasets-list.component";

@NgModule({
  imports: [
    CommonModule,
    DatasetsModule,
    FeaturesModule,
    GridModule,
    LoadersModule,
    MatButtonModule,
    MatDialogModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  exports: [DatasetsComponent, DatasetsListComponent],
  entryComponents: [DatasetRowMenuRendererComponent],
  declarations: [
    DatasetsComponent,
    DatasetsListComponent,
    DatasetRowMenuRendererComponent
  ]
})
export class DatasetListModule {}
