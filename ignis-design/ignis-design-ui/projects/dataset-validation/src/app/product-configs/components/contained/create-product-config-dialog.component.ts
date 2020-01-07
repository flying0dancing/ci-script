import { Component, Inject, OnDestroy } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs';
import { filter, map, withLatestFrom } from 'rxjs/operators';
import { FIELD_ERRORS } from '../../../core/forms/field.validators';
import { ApiError } from '../../../core/utilities/interfaces/errors.interface';
import * as ProductActions from '../../actions/product-configs.actions';
import {
  PRODUCTS_ERROR_STATE,
  PRODUCTS_LOADED_STATE
} from '../../reducers/product-configs.selectors';

@Component({
  selector: 'dv-add-product-config',
  templateUrl: './create-product-config-dialog.component.html'
})
export class CreateProductConfigDialogComponent implements OnDestroy {
  productForm = this.fb.group({
    name: null,
    version: '1'
  });
  nameControl = this.productForm.controls.name;
  fieldErrors = FIELD_ERRORS;

  private errors$ = this.store.select(PRODUCTS_ERROR_STATE);
  private loadedSuccessfully$ = this.store.select(PRODUCTS_LOADED_STATE).pipe(
    withLatestFrom(this.errors$),
    filter(([loaded, errorResponse]) => loaded && errorResponse.hasNoErrors())
  );
  private closeDialogSubscription: Subscription;

  constructor(
    private fb: FormBuilder,
    private store: Store<any>,
    public dialogRef: MatDialogRef<CreateProductConfigDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data
  ) {}

  ngOnDestroy(): void {
    if (this.closeDialogSubscription) {
      this.closeDialogSubscription.unsubscribe();
    }
  }

  createProduct(): void {
    this.errors$
      .pipe(
        filter(errorResponse => errorResponse.hasErrors()),
        map(errorResponse => errorResponse.errors)
      )
      .subscribe(errors => this.addFormErrors(errors));

    const name: string = this.nameControl.value;
    const version: number = this.productForm.controls.version.value;

    this.store.dispatch(
      new ProductActions.Create({ name, version, tableIds: [] })
    );

    this.closeDialogSubscription = this.loadedSuccessfully$.subscribe(() =>
      this.dialogRef.close()
    );
  }

  cancel(): void {
    this.dialogRef.close();
  }

  private addFormErrors(errors: ApiError[]) {
    const error = errors.find(err => err.errorCode === 'name');

    if (error) {
      this.nameControl.setErrors({ [this.fieldErrors]: error.errorMessage });
      this.nameControl.markAsTouched();
    }
  }
}
