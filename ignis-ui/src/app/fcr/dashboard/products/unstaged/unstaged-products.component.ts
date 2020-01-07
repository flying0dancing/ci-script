import * as ProductsActions from "@/core/api/products/products.actions";
import { Product } from "@/core/api/products/products.interfaces";
import { NAMESPACE } from "@/core/api/products/products.reducer";
import { ConfirmDialogService } from "@/shared/dialogs";
import { ChangeDetectionStrategy, Component, Input } from "@angular/core";
import { MatDialog } from "@angular/material/dialog";
import { Store } from "@ngrx/store";

@Component({
  selector: "app-unstaged-products",
  templateUrl: "./unstaged-products.component.html",
  styleUrls: ["./unstaged-products.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class UnstagedProductsComponent {
  @Input() products: Product[];

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private confirmDialogService: ConfirmDialogService
  ) {}

  delete(id: number) {
    let confirmDialogRef = this.confirmDialogService.confirm({
      title: "Are you sure?",
      content: "This product configuration will be deleted",
      confirmButtonText: "Yes",
      cancelButtonText: "No"
    });

    confirmDialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.store.dispatch(
          new ProductsActions.Delete({ reducerMapKey: NAMESPACE, id })
        );
      }
      confirmDialogRef = undefined;
    });
  }
}
