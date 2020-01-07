import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { filter, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { ErrorDialogComponent } from '../../core/dialogs/components/error-dialog.component';
import { Schema } from '../../schemas';
import { GetAll, Update } from '../actions/product-configs.actions';
import { ProductConfig } from '../interfaces/product-config.interface';
import { UpdateProductConfigRequest } from '../interfaces/update-product-request.interface';
import { getProduct, PRODUCTS_LOADING_STATE } from '../reducers/product-configs.selectors';
import { ValidateProductDialogComponent } from './contained/validate-product-dialog.component';

@Component({
  selector: 'dv-product-config-container',
  templateUrl: './product-config-container.component.html',
  styleUrls: ['./product-config-container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductConfigContainerComponent implements OnInit, OnDestroy {
  product: ProductConfig;
  schemas: Schema[] = [];

  loading$: Observable<boolean> = this.store.pipe(
    select(PRODUCTS_LOADING_STATE)
  );
  loaded$: Observable<boolean> = this.loading$.pipe(
    filter(loading => !loading)
  );

  idParam$: Observable<number> = this.route.paramMap.pipe(
    map(params => parseInt(params.get('id'), 10))
  );
  product$: Observable<ProductConfig> = this.loaded$.pipe(
    withLatestFrom(this.idParam$),
    switchMap(([loaded, id]) => this.store.pipe(select(getProduct(id))))
  );

  productSubscription: Subscription;

  editVersion = val => {
    this.store.dispatch(
      new Update(this.product.id, new UpdateProductConfigRequest(null, val))
    );
  };

  editName = name => {
    this.store.dispatch(
      new Update(this.product.id, new UpdateProductConfigRequest(name, null))
    );
  };

  constructor(
    private route: ActivatedRoute,
    private store: Store<any>,
    private fb: FormBuilder,
    private dialog: MatDialog
  ) {
    this.productSubscription = this.product$
      .pipe(filter(product => !product))
      .subscribe(() => this.store.dispatch(new GetAll()));
  }

  ngOnDestroy(): void {
    this.productSubscription.unsubscribe();
  }

  ngOnInit(): void {
    this.product$.pipe(filter(product => !!product)).subscribe(product => {
      this.product = product;
      this.schemas = product.schemas;
    });
  }

  validateProduct() {
    this.dialog.open(ValidateProductDialogComponent, {
      width: '650px',
      height: '550px',
      disableClose: true,
      data: {
        productId: this.product.id,
        productName: this.product.name
      }
    });
  }

  private openErrorDialog(message: string): void {
    let dialogRef = this.dialog.open(ErrorDialogComponent, {
      width: '250px',
      data: { message: message }
    });

    dialogRef.afterClosed().subscribe(() => {
      dialogRef = undefined;
    });
  }

  checkSchemaCurrentCount() {
    return this.schemas.length > 0;
  }
}
