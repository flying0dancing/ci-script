import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { select, Store } from '@ngrx/store';
import { combineLatest, Subject } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';
import * as PipelineActions from '../../pipelines/actions/pipelines.actions';
import { getPipeline, PIPELINES_LOADING_STATE } from '../../pipelines/selectors/pipelines.selector';
import * as ProductActions from '../../product-configs/actions/product-configs.actions';
import { getProduct, PRODUCTS_LOADING_STATE } from '../../product-configs/reducers/product-configs.selectors';
import * as PipelineStepTestRowsActions from '../actions/pipeline-step-test-rows.actions';

import * as PipelineStepTestActions from '../actions/pipeline-step-test.actions';
import * as selectors from '../reducers';

@Component({
  selector: 'dv-pipeline-step-test-page-container',
  templateUrl: './pipeline-step-test-page-container.component.html'
})
export class PipelineStepTestPageContainerComponent implements OnDestroy {
  productsLoading$ = this.store.select(PRODUCTS_LOADING_STATE);

  pipelineLoading$ = this.store.select(PIPELINES_LOADING_STATE);

  productIdParam$ = this.route.paramMap.pipe(
    map(params => parseInt(params.get('productId'), 10))
  );

  pipelineIdParam$ = this.route.paramMap.pipe(
    map(params => parseInt(params.get('pipelineId'), 10))
  );

  pipelineStepTestIdParam$ = this.route.paramMap.pipe(
    map(params => parseInt(params.get('id'), 10))
  );

  product$ = this.productIdParam$.pipe(
    switchMap(id => this.store.pipe(select(getProduct(id))))
  );

  pipeline$ = this.pipelineIdParam$.pipe(
    switchMap(id => this.store.select(getPipeline(id)))
  );

  pipelineStepTest$ = this.pipelineStepTestIdParam$.pipe(
    switchMap(id => this.store.select(selectors.getPipelineStepTest(id)))
  );

  pipelineStepTestLoading$ = this.pipelineStepTestIdParam$.pipe(
    switchMap(id => this.store.select(selectors.getPipelineStepTestLoading(id)))
  );

  private unsubscribe$ = new Subject();

  constructor(private store: Store<any>, private route: ActivatedRoute) {
    combineLatest([this.productIdParam$, this.pipelineIdParam$])
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe(([productId, pipelineId]) => {
        this.store.dispatch(new ProductActions.GetOne(productId));
        this.store.dispatch(new PipelineActions.GetOne(pipelineId));
      });

    this.pipelineStepTestIdParam$
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe(id => {
        this.store.dispatch(new PipelineStepTestActions.GetOne(id));
        this.store.dispatch(new PipelineStepTestRowsActions.GetTestInputRows(id));
        this.store.dispatch(new PipelineStepTestRowsActions.GetTestExpectedRows(id));
      });
  }

  ngOnDestroy() {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }
}
