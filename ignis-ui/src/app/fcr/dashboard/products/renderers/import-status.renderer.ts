import { Component } from "@angular/core";
import { ICellRendererParams } from "ag-grid";
import { ICellRendererAngularComp } from "ag-grid-angular";

@Component({
  selector: "app-import-status-renderer",
  templateUrl: "./import-status.renderer.html",
  styleUrls: ["./import-status.renderer.scss"]
})
export class ImportStatusRendererComponent implements ICellRendererAngularComp {
  importStatus: string;

  refresh(params: ICellRendererParams): boolean {
    return false;
  }

  agInit(params: ICellRendererParams) {
    this.importStatus = params.value;
  }
}
