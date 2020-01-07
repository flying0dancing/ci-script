import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  Output
} from '@angular/core';
import {
  ColDef,
  GridApi,
  GridOptions,
  GridReadyEvent,
  RowDataChangedEvent
} from 'ag-grid';
import { Observable, Subscription } from 'rxjs';
import { createDefaultGridOptions } from '../../../../../core/grid/grid.functions';
import { Field } from '../../../../../schemas';
import { emptyExampleFieldIfNull } from '../../../../functions/schema-details.functions';
import {
  ExampleField,
  RuleExample
} from '../../../../interfaces/rule-example.interface';
import { BooleanFieldEditorComponent } from './boolean-field-editor.component';
import { ContextFieldHeaderRendererComponent } from './context-field-header-renderer.component';
import { DecimalFieldEditorComponent } from './decimal-field-editor.component';
import { ExampleFieldRendererComponent } from './example-field-renderer.component';
import { ExpectedResultEditorComponent } from './expected-result-editor.component';
import { FormattedDateFieldEditorComponent } from './formatted-date-field-editor.component';
import { NumberFieldEditorComponent } from './number-field-editor.component';
import { RemoveExampleRendererComponent } from './remove-example-renderer.component';
import { StringFieldEditorComponent } from './string-field-editor.component';
import { TestResultRendererComponent } from './test-result-renderer.component';

const EXAMPLES_PAGE_SIZE = 8;

@Component({
  selector: 'dv-examples-grid',
  templateUrl: 'rule-examples-grid.component.html',
  styleUrls: ['rule-examples-grid.component.scss']
})
export class RuleExamplesGridComponent implements OnDestroy {
  @Input() ruleExamples: RuleExample[];
  @Input() contextFields$: Observable<Field[]>;
  @Output() deleteExampleEvent: EventEmitter<any> = new EventEmitter();
  @Input() gotoLastExamplePageEvent: EventEmitter<any>;

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    suppressRowClickSelection: true,
    suppressCellSelection: true,
    paginationPageSize: EXAMPLES_PAGE_SIZE,
    overlayLoadingTemplate: 'Loading...',
    overlayNoRowsTemplate: 'No Examples Found',
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent),
    onGridSizeChanged: () => this.updateGridSize(),
    onRowDataChanged: (event: RowDataChangedEvent) => this.goToLastPage(event)
  };

  resultsColumnDefs: ColDef[] = [
    {
      headerName: 'No.',
      headerClass: 'narrow-margin-column',
      valueGetter: params => Number.parseInt(params.node.id, 10) + 1,
      valueFormatter: params => `${params.value}. `,
      filter: 'agNumberColumnFilter',
      cellClass: 'narrow-margin-column',
      width: 50,
      maxWidth: 50,
      minWidth: 50,
      pinned: true,
      suppressResize: true
    },
    {
      headerName: 'Expected',
      headerTooltip: 'Expected Result',
      headerClass: 'narrow-margin-column',
      field: 'expectedResult',
      editable: true,
      cellEditorFramework: ExpectedResultEditorComponent,
      cellClass: 'narrow-margin-column',
      singleClickEdit: true,
      minWidth: 160,
      pinned: true,
      suppressResize: true,
      onCellValueChanged: params =>
        RuleExamplesGridComponent.resetResultForExpectedResultChanges(params)
    },
    {
      headerName: 'Result',
      headerTooltip: 'Shows if the expected and actual results match',
      headerClass: 'narrow-margin-column',
      field: 'actualResult',
      cellRendererFramework: TestResultRendererComponent,
      cellClass: 'narrow-margin-column',
      width: 105,
      minWidth: 105,
      maxWidth: 105,
      pinned: true,
      suppressResize: true
    },
    {
      headerTooltip: 'Remove Example',
      cellRendererFramework: RemoveExampleRendererComponent,
      cellClass: 'remove-example narrow-margin-column',
      cellRendererParams: { deleteExampleEvent: this.deleteExampleEvent },
      width: 60,
      minWidth: 60,
      maxWidth: 60,
      pinned: true,
      suppressResize: true,
      suppressMenu: true,
      suppressSorting: true,
      suppressToolPanel: true
    }
  ];
  columnDefs: ColDef[] = [];

  private gridApi: GridApi;
  private contextFieldsSubscription: Subscription;
  private gotoLastExamplePageSubscription: Subscription;
  private atLastPage = false;

  private static selectFieldEditor(field: Field) {
    switch (field.type) {
      case 'boolean':
        return BooleanFieldEditorComponent;
      case 'string':
        return StringFieldEditorComponent;
      case 'timestamp':
      case 'date':
        return FormattedDateFieldEditorComponent;
      case 'decimal':
        return DecimalFieldEditorComponent;
      case 'double':
      case 'float':
      case 'int':
      case 'long':
        return NumberFieldEditorComponent;
      default:
        return null;
    }
  }

  private static resetResultForExpectedResultChanges(params): void {
    if (params.oldValue !== params.newValue) {
      RuleExamplesGridComponent.resetActualResult(params);
    }
  }

  private static resetResultForFieldChanges(params): void {
    const oldField: ExampleField = emptyExampleFieldIfNull(params.oldValue);
    const newField: ExampleField = emptyExampleFieldIfNull(params.newValue);

    if (
      RuleExamplesGridComponent.exampleFieldsAreDifferent(oldField, newField)
    ) {
      RuleExamplesGridComponent.resetActualResult(params);
    }
  }

  private static exampleFieldsAreDifferent(
    oldField: ExampleField,
    newField: ExampleField
  ): boolean {
    return (
      oldField.value !== newField.value || oldField.error !== newField.error
    );
  }

  private static resetActualResult(params) {
    params.data.actualResult = null;
    params.node.updateData(params.data);
  }

  ngOnDestroy(): void {
    this.contextFieldsSubscription.unsubscribe();
    this.gotoLastExamplePageSubscription.unsubscribe();
  }

  private onGridReady(readyEvent: GridReadyEvent): void {
    this.gridApi = readyEvent.api;

    if (this.ruleExamples.length < 1) {
      this.gridApi.showNoRowsOverlay();
    }
    this.contextFieldsSubscription = this.contextFields$.subscribe(
      contextFields => this.addContextFieldsToGrid(contextFields)
    );

    this.gotoLastExamplePageSubscription = this.gotoLastExamplePageEvent.subscribe(
      () => (this.atLastPage = false)
    );

    this.gridApi.paginationGoToFirstPage();
  }

  private updateGridSize() {
    this.gridApi.showLoadingOverlay();

    setTimeout(() => {
      this.gridApi.sizeColumnsToFit();
      this.gridApi.hideOverlay();
      if (this.ruleExamples.length < 1) {
        this.gridApi.showNoRowsOverlay();
      }
    }, 300);
  }

  private goToLastPage(event: RowDataChangedEvent) {
    if (!this.atLastPage) {
      event.api.paginationGoToLastPage();
      this.atLastPage = true;
    }
  }

  private addContextFieldsToGrid(contextFields: Field[]) {
    const contextFieldColDefs = contextFields.map(field => {
      return {
        headerName: field.name,
        headerComponentFramework: ContextFieldHeaderRendererComponent,
        headerComponentParams: { field },
        field: `exampleFields.${field.name}`,
        cellRendererFramework: ExampleFieldRendererComponent,
        cellEditorFramework: RuleExamplesGridComponent.selectFieldEditor(field),
        cellEditorParams: { field },
        editable: true,
        singleClickEdit: true,
        onCellValueChanged: params =>
          RuleExamplesGridComponent.resetResultForFieldChanges(params)
      } as ColDef;
    });
    this.columnDefs = [...this.resultsColumnDefs, ...contextFieldColDefs];
  }
}
