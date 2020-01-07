import { ChangeDetectionStrategy, Component, NgZone, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { select, Store } from '@ngrx/store';
import { ShortcutsService } from '../../core/shortcuts/shortcuts.service';
import * as productConfigs from '../actions/product-configs.actions';
import { GetAll } from '../actions/product-configs.actions';
import { PRODUCT_ENTITIES, PRODUCTS_LOADING_STATE } from '../reducers/product-configs.selectors';
import { ProductConfigsRepository } from '../services/product-configs.repository';
import { CreateProductConfigDialogComponent } from './contained/create-product-config-dialog.component';
import { UploadFileDialogComponent } from './dialog/upload-file-dialog.component';

@Component({
  selector: 'dv-product-configs-container',
  templateUrl: './product-configs-container.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductConfigsContainerComponent implements OnInit {
  productConfigs$ = this.store.pipe(select(PRODUCT_ENTITIES));
  loading$ = this.store.pipe(select(PRODUCTS_LOADING_STATE));

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private shortcutsService: ShortcutsService,
    private productsService: ProductConfigsRepository,
    private ngZone: NgZone
  ) {}

  ngOnInit() {
    this.store.dispatch(new productConfigs.GetAll());

    this.setupShortcuts();
  }

  private setupShortcuts() {
    this.shortcutsService.keepCheatSheetShortcutsOnly();
  }

  addProductConfig() {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(CreateProductConfigDialogComponent, {
        width: '480px',
        maxHeight: '850px',
        disableClose: true,
        data: {}
      });

      dialogRef.afterClosed().subscribe(() => {
        dialogRef = undefined;
      });
    });
  }

  openUploadDialog() {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(UploadFileDialogComponent, {
        width: '680px',
        maxHeight: '850px',
        disableClose: false,
        data: {
          title: 'Product',
          uploadFunction: formData => this.productsService.upload(formData),
          onUploadSuccess: () => this.store.dispatch(new GetAll())
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        dialogRef = undefined;
      });
    });
  }
}
