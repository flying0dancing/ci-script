import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatIconModule } from "@angular/material/icon";
import { AgGridModule } from "ag-grid-angular/main";

import { GridComponent } from "./grid.component";

@NgModule({
  imports: [
    AgGridModule.withComponents([]),
    CommonModule,
    MatButtonModule,
    MatIconModule
  ],
  declarations: [GridComponent],
  exports: [GridComponent]
})
export class GridModule {}
