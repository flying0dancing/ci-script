import { Component, Input, NgZone, OnChanges, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material';
import { select, Store } from '@ngrx/store';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import * as pipelineSelectors from '../../pipelines/selectors/pipelines.selector';
import { UploadFileDialogComponent } from '../../product-configs/components/dialog/upload-file-dialog.component';
import * as productSelectors from '../../product-configs/reducers/product-configs.selectors';
import * as pipelineStepTestRowActions from '../actions/pipeline-step-test-rows.actions';
import * as pipelineStepTestActions from '../actions/pipeline-step-test.actions';
import { GridCellEditEvent } from '../interfaces/grid-cell-edit-event.interface';
import { GridRowDeleteEvent } from '../interfaces/grid-row-delete-event.interface';
import * as stepTestSelectors from '../reducers';
import { PipelineStepTestsRepository } from '../services/pipeline-step-tests.repository';
import { getPipelineStepTestStepSchemas, getSchemaOutId } from '../utilities/pipeline-step-tests.utilities';

@Component({
  selector: 'dv-pipeline-step-test-editor',
  templateUrl: './pipeline-step-test-editor.component.html',
  styleUrls: ['./pipeline-step-test-editor.component.scss']
})
export class PipelineStepTestEditorComponent implements OnChanges {
  @Input() productId: number;

  @Input() pipelineId: number;

  @Input() pipelineStepTestId: number;

  productIdSubject = new BehaviorSubject<number>(null);

  productId$ = this.productIdSubject.asObservable();

  product$ = this.productId$.pipe(
    switchMap(id => this.store.pipe(select(productSelectors.getProduct(id))))
  );

  productsLoaded$ = this.store.select(productSelectors.PRODUCTS_LOADED_STATE);

  pipelineIdSubject = new BehaviorSubject<number>(null);

  pipelineId$ = this.pipelineIdSubject.asObservable();

  pipeline$ = this.pipelineId$.pipe(
    switchMap(id => this.store.pipe(select(pipelineSelectors.getPipeline(id))))
  );

  pipelineStepTestIdSubject = new BehaviorSubject<number>(null);

  pipelineStepTestId$ = this.pipelineStepTestIdSubject.asObservable();

  pipelineStepTest$ = this.pipelineStepTestId$.pipe(
    switchMap(id =>
      this.store.select(stepTestSelectors.getPipelineStepTest(id))
    )
  );

  pipelineTestInputRows$ = this.store.select(stepTestSelectors.getTestInputRows);

  pipelineTestInputRowsLoading$ = combineLatest([
    this.store.select(stepTestSelectors.getTestInputRowsLoading),
    this.store.select(stepTestSelectors.getUpdateRowCellLoading)])
    .pipe(
      map(([rowsLoading, updateLoading]) => rowsLoading || updateLoading)
    );

  pipelineTestOutputRows$ = this.store.select(stepTestSelectors.getOutputData);

  pipelineTestOutputRowsLoading$ = combineLatest([
    this.store.select(stepTestSelectors.getTestExpectedRowsLoading),
    this.store.select(stepTestSelectors.getUpdateExpectedRowCellLoading)])
    .pipe(
      map(([rowsLoading, updateLoading]) => rowsLoading || updateLoading)
    );

  pipelineStepTestLoading$ = this.pipelineStepTestId$.pipe(
    switchMap(id =>
      this.store.select(stepTestSelectors.getPipelineStepTestLoading(id))
    )
  );

  inputDataRowCreating$ = this.pipelineStepTestId$.pipe(
    switchMap(id =>
      this.store.select(stepTestSelectors.getInputDataRowCreating(id))
    )
  );

  expectedDataRowCreating$ = this.pipelineStepTestId$.pipe(
    switchMap(id =>
      this.store.select(stepTestSelectors.getExpectedDataRowCreating(id))
    )
  );

  schemas$ = combineLatest([this.pipelineTestInputRows$, this.product$]).pipe(
    map(([pipelineInputRows, product]) =>
      getPipelineStepTestStepSchemas(pipelineInputRows, product)
    )
  );

  selectedTabIndexSubject = new BehaviorSubject(0);

  selectedTabIndex$ = this.selectedTabIndexSubject.asObservable();

  selectedInputSchema$ = combineLatest([
    this.selectedTabIndex$,
    this.schemas$
  ]).pipe(
    map(([selectedTabIndex, schemas]) => schemas && schemas[selectedTabIndex])
  );

  pipelineStepSchemaOutId$ = combineLatest([
    this.pipeline$,
    this.pipelineStepTest$
  ]).pipe(
    map(([pipeline, pipelineStepTest]) =>
      getSchemaOutId(pipelineStepTest, pipeline)
    )
  );

  pipelineStepSchemaOut$ = combineLatest([
    this.pipelineStepSchemaOutId$,
    this.product$
  ]).pipe(
    map(([schemaOutId, product]) =>
      product.schemas.find(s => s.id === schemaOutId)
    )
  );

  pipelineStepTestRunning$ = this.pipelineStepTestId$.pipe(
    switchMap(id => this.store.select(stepTestSelectors.getRunning(id)))
  );
  exportExpectedMenu: any;

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private pipelineStepTestsService: PipelineStepTestsRepository,
    private ngZone: NgZone) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes.productId) {
      const productId = changes.productId.currentValue;

      this.productIdSubject.next(productId);
    }

    if (changes.pipelineId) {
      const pipelineId = changes.pipelineId.currentValue;

      this.pipelineIdSubject.next(pipelineId);
    }

    if (changes.pipelineStepTestId) {
      const pipelineStepTestId = changes.pipelineStepTestId.currentValue;

      this.pipelineStepTestIdSubject.next(pipelineStepTestId);
    }
  }

  createInputDataRow(schemaId: number) {
    this.store.dispatch(
      new pipelineStepTestRowActions.CreateInputDataRow(
        { schemaId },
        this.pipelineStepTestId
      )
    );
  }

  createExpectedDataRow(schemaId: number) {
    this.store.dispatch(
      new pipelineStepTestRowActions.CreateExpectedDataRow(
        { schemaId },
        this.pipelineStepTestId
      )
    );
  }

  handleCellEdit(data: GridCellEditEvent) {
    if (data.inputRow) {
      this.store.dispatch(
        new pipelineStepTestRowActions.UpdateInputRowCellData(
          data.pipelineStepId, data.rowId, data.cell.id, { inputDataValue: data.cell.data }));
    } else {
      this.store.dispatch(
        new pipelineStepTestRowActions.UpdateExpectedRowCellData(
          data.pipelineStepId, data.rowId, data.cell.id, { inputDataValue: data.cell.data }));
    }
  }

  handleInputRowDelete(data: GridRowDeleteEvent) {
    this.store.dispatch(
      new pipelineStepTestRowActions.DeleteInputDataRow(
        data.pipelineStepId,
        data.rowId
      )
    );
  }

  handleExpectedRowDelete(data: GridRowDeleteEvent) {
    this.store.dispatch(
      new pipelineStepTestRowActions.DeleteExpectedDataRow(
        data.pipelineStepId,
        data.rowId
      )
    );
  }

  onTabChange(index: number) {
    this.selectedTabIndexSubject.next(index);
  }

  runTest() {
    this.store.dispatch(
      new pipelineStepTestActions.Run(this.pipelineStepTestId)
    );
  }

  uploadDataRowDialog(schemaId: number, importType: 'input' | 'expected') {
    this.ngZone.run(() => {
      let dialogRef = this.dialog.open(UploadFileDialogComponent, {
        width: '680px',
        maxHeight: '850px',
        disableClose: false,
        data: {
          title: 'Test CSV',
          uploadFunction: formData => this.pipelineStepTestsService.uploadInputDataRow(this.pipelineStepTestId,
            schemaId, formData, importType),
          onUploadSuccess: () => importType === 'input'
            ? this.store.dispatch(new pipelineStepTestRowActions.GetTestInputRows(this.pipelineStepTestId))
            : this.store.dispatch(new pipelineStepTestRowActions.GetTestExpectedRows(this.pipelineStepTestId))
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        dialogRef = undefined;
      });
    });
  }

  downloadRowData(schemaId: number, exportType: 'input' | 'actual' | 'expected') {
    const url = this.pipelineStepTestsService.exportDataRowDownloadUrl(
      this.pipelineStepTestId,
      schemaId,
      exportType);
    window.open(url);

  }
}
