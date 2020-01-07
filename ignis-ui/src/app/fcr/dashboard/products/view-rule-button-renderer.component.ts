import { ProductItemGroupKey } from "@/fcr/dashboard/products/product-schema.interface";
import { ChangeDetectionStrategy, Component } from "@angular/core";
import { Router } from "@angular/router";
import { ICellRendererParams } from "ag-grid";
import { ICellRendererAngularComp } from "ag-grid-angular";

@Component({
  selector: "app-view-rule",
  templateUrl: "./view-rule-button-renderer.component.html",
  styleUrls: ["./view-rule-button-renderer.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ViewRuleButtonRendererComponent
  implements ICellRendererAngularComp {
  name: string;
  hover: boolean;
  isSchema: boolean;
  isPipeline: boolean;

  private productId: number;
  private itemId: number;

  constructor(private router: Router) {}

  refresh(): boolean {
    return false;
  }

  agInit(params: ICellRendererParams): void {
    if (!params.node.group) {
      this.name = params.getValue();
      this.productId = params.data.productId;
      this.itemId = params.data.itemId;
      this.isSchema = params.data.group === ProductItemGroupKey.SCHEMA;
      this.isPipeline = params.data.group === ProductItemGroupKey.PIPELINE;
    }
  }

  openRule() {
    const queryParams = {
      productId: this.productId,
      schemaId: this.itemId
    };
    this.router.navigate(["rules"], { queryParams });
  }
}
