import { RowMenuItem } from "@/fcr/dashboard/shared/grid/column-definitions/row-menu-item.interface";
import { ICellRendererParams } from "ag-grid";
import { AgRendererComponent } from "ag-grid-angular";

export abstract class RowMenuRendererComponent implements AgRendererComponent {
  menuTooltip: string;
  rowMenuItems: RowMenuItem[] = [];

  refresh(params: any): boolean {
    return false;
  }

  abstract agInit(params: ICellRendererParams): void;
}
