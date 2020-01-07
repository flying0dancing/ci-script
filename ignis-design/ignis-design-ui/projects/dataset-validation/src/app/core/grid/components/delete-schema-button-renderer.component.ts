import { Component, NgZone, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as ProductActions from '../../../product-configs/actions/product-configs.actions';
import { ProductConfig } from '../../../product-configs/interfaces/product-config.interface';
import { PRODUCT_ENTITIES } from '../../../product-configs/reducers/product-configs.selectors';
import { DeleteButtonRendererComponent } from './delete-button-renderer.component';

@Component({
  templateUrl: './delete-schema-button-renderer.component.html'
})
export class DeleteSchemaButtonRendererComponent
  extends DeleteButtonRendererComponent
  implements OnInit {
  constructor(
    protected dialog: MatDialog,
    protected store: Store<any>,
    protected ngZone: NgZone
  ) {
    super(dialog, store, ngZone);
  }

  products$: Observable<ProductConfig[]> = this.store.select(PRODUCT_ENTITIES);

  hasProducts$: Observable<boolean> = this.products$.pipe(
    filter((products: ProductConfig[]) => !!products),
    map((products: ProductConfig[]) => this.schemaInProducts(products))
  );

  tooltip$ = this.hasProducts$.pipe(
    map((hasProducts: boolean) =>
      hasProducts
        ? 'Cannot delete schema because it has associated products'
        : 'Delete'
    )
  );

  ngOnInit(): void {
    this.store.dispatch(new ProductActions.GetAll());
  }

  private schemaInProducts(products: ProductConfig[]): boolean {
    return !!products.find(product =>
      this.schemaInProduct(this.params.data.id, product)
    );
  }

  private schemaInProduct(schemaId: number, product: ProductConfig): boolean {
    return !!product.schemas.find(
      productSchema => productSchema.id === schemaId
    );
  }
}
