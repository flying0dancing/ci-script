import * as PipelineActions from '@/core/api/pipelines/pipelines.actions';
import { Pipeline } from '@/core/api/pipelines/pipelines.interfaces';
import { NAMESPACE as PIPELINE_NAMESPACE } from '@/core/api/pipelines/pipelines.reducer';
import * as PipelinesSelectors from '@/core/api/pipelines/pipelines.selectors';
import * as ProductsActions from '@/core/api/products/products.actions';
import { Product } from '@/core/api/products/products.interfaces';
import { NAMESPACE } from '@/core/api/products/products.reducer';
import { ProductRowItem, productToProductSchemaAndPipelines } from '@/fcr/dashboard/products/product-schema.interface';
import { ProductUploadDialogComponent } from '@/fcr/dashboard/products/product-upload-dialog.component';
import { DialogsConstants } from '@/shared/dialogs';
import { flatten } from '@/shared/utilities/array.utilities';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { map, take } from 'rxjs/internal/operators';
import { filter, pairwise } from 'rxjs/operators';
import * as ProductsSelectors from './products.selectors';

@Component({
  selector: 'app-products',
  templateUrl: './products.component.html',
  styleUrls: ['./products.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductsComponent implements OnInit, OnDestroy {
  products$: Observable<Product[]> = this.store.select(
    ProductsSelectors.getProductsCollection
  );

  pipelines: Pipeline[] = [];
  pipelines$: Observable<Pipeline[]> = this.store.select(
    PipelinesSelectors.getPipelines
  );

  unstagedProducts$: Observable<Product[]> = this.products$.pipe(
    map(products =>
      products.filter(product => !this.productHasDatasets(product))
    )
  );

  productSchemas$: Observable<ProductRowItem[]> = this.products$.pipe(
    map(products => {
      return flatten(products.map(productToProductSchemaAndPipelines));
    })
  );

  productsGetLoading$: Observable<any> = this.store.select(
    ProductsSelectors.getProductsLoading
  );

  private importJobState$: Observable<any> = this.store.select(
    ProductsSelectors.importJobState
  );

  private importJobStarted$ = this.importJobState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev && curr && !prev.running && curr.running)
  );
  private importJobCompleted$ = this.importJobState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev && curr && prev.running && !curr.running)
  );

  importJobStartedSubscription: Subscription;
  importJobCompletedSubscription: Subscription;

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    public snackbar: MatSnackBar
  ) {}

  ngOnInit() {
    this.store.dispatch(
      new ProductsActions.Empty({ reducerMapKey: NAMESPACE })
    );
    this.store.dispatch(new ProductsActions.Get({ reducerMapKey: NAMESPACE }));

    this.store.dispatch(
      new PipelineActions.Empty({ reducerMapKey: PIPELINE_NAMESPACE })
    );
    this.store.dispatch(
      new PipelineActions.Get({ reducerMapKey: PIPELINE_NAMESPACE })
    );
    this.store.dispatch(
      new PipelineActions.GetInvocations({
        reducerMapKey: PIPELINE_NAMESPACE
      }));
    this.pipelines$.subscribe((pipelines) => (this.pipelines = pipelines));

    this.registerStartImportJobSubscriptions();
  }

  handleUploadProductButtonClick() {
    let productUploadDialogRef = this.dialog.open(
      ProductUploadDialogComponent,
      {
        width: DialogsConstants.WIDTHS.LARGE
      }
    );

    productUploadDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (productUploadDialogRef = undefined));
  }

  refreshProducts() {
    this.store.dispatch(new ProductsActions.Get({ reducerMapKey: NAMESPACE }));
    this.store.dispatch(
      new PipelineActions.Get({ reducerMapKey: PIPELINE_NAMESPACE })
    );
  }

  private productHasDatasets(product: Product): boolean {
    return !!product.schemas.find(table => table.hasDatasets);
  }

  private registerStartImportJobSubscriptions() {
    this.importJobStartedSubscription = this.importJobStarted$.subscribe(() =>
      this.handleStartedImportJob()
    );
    this.importJobCompletedSubscription = this.importJobCompleted$.subscribe(
      () => this.handleCompletedImportJob()
    );
  }

  private handleStartedImportJob() {
    this.snackbar.open('Import Job Started', undefined, { duration: 3000 });
  }

  private handleCompletedImportJob() {
    this.snackbar.open('Import Job Completed', undefined, { duration: 3000 });
  }

  ngOnDestroy() {
    this.importJobStartedSubscription.unsubscribe();
    this.importJobCompletedSubscription.unsubscribe();
  }
}
