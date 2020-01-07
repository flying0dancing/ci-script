import * as ProductsActions from "@/core/api/products/products.actions";
import { Product } from "@/core/api/products/products.interfaces";
import { NAMESPACE as PRODUCTS } from "@/core/api/products/products.reducer";
import { Rule } from "@/core/api/tables/rule.interface";
import { Table } from "@/core/api/tables/tables.interfaces";
import {
  getProductsCollection,
  getProductsLoading
} from "@/fcr/dashboard/products/products.selectors";
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { Component, OnDestroy, OnInit } from "@angular/core";
import { AbstractControl, FormBuilder, FormGroup } from "@angular/forms";
import { ActivatedRoute, Router } from "@angular/router";
import { Store } from "@ngrx/store";
import {
  ColDef,
  ColumnResizedEvent,
  GridApi,
  GridOptions,
  GridReadyEvent
} from "ag-grid";
import { combineLatest, Subscription } from "rxjs";
import { filter, map } from "rxjs/operators";

@Component({
  selector: "app-rule-details",
  templateUrl: "./rule-details.component.html",
  styleUrls: ["./rule-details.component.scss"],
  providers: [ LocaleTimePipe ]
})
export class RuleDetailsComponent implements OnInit, OnDestroy {
  customStyle = { height: "760px" };

  gridOptions: GridOptions = {
    enableColResize: true,
    enableFilter: true,
    enableSorting: true,
    overlayLoadingTemplate: "Loading Rules",
    overlayNoRowsTemplate: "No Rules Found",
    pagination: true,
    rowClass: "rule-row",
    onGridReady: params => this.setupGrid(params),
    onColumnResized: columnResizeEvent => this.resizeRow(columnResizeEvent)
  };

  columnDefs: ColDef[] = [
    {
      headerName: "Regulator Reference",
      headerClass: "header-wrap",
      headerTooltip: "Regulator Reference",
      field: "ruleId",
      tooltipField: "ruleId",
      filter: "agTextColumnFilter",
      width: 160
    },
    {
      headerName: "Name",
      headerTooltip: "Name",
      field: "name",
      tooltipField: "name",
      filter: "agTextColumnFilter",
      width: 160
    },
    {
      headerName: "Version",
      headerTooltip: "Version",
      field: "version",
      filter: "agNumberColumnFilter",
      width: 120
    },
    {
      headerName: "Type",
      headerTooltip: "Type",
      field: "validationRuleType",
      filter: "agTextColumnFilter",
      width: 120
    },
    {
      headerName: "Severity",
      headerTooltip: "Severity",
      field: "validationRuleSeverity",
      filter: "agTextColumnFilter",
      width: 120
    },
    {
      headerName: "Start Date",
      headerClass: "header-wrap",
      headerTooltip: "Start Date",
      field: "startDate",
      filter: "agDateColumnFilter",
      valueFormatter: params => this.localeTime.transform(params.value),
      width: 130
    },
    {
      headerName: "End Date",
      headerClass: "header-wrap",
      headerTooltip: "End Date",
      field: "endDate",
      filter: "agDateColumnFilter",
      valueFormatter: params => this.localeTime.transform(params.value),
      width: 130
    },
    {
      headerName: "Description",
      headerTooltip: "Description",
      field: "description",
      tooltipField: "description",
      filter: "agTextColumnFilter",
      autoHeight: true,
      cellClass: "rule-description",
      suppressSorting: true,
      width: 480
    },
    {
      headerName: "Expression",
      headerTooltip: "Expression",
      field: "expression",
      tooltipField: "expression",
      filter: "agTextColumnFilter",
      cellClass: "rule-expression",
      autoHeight: true,
      suppressSorting: true,
      width: 440
    }
  ];

  formGroup: FormGroup = this.formBuilder.group({
    selectedProductId: null,
    selectedSchemaId: null
  });
  products: Product[] = [];
  selectedProductSchemas: Table[] = [];
  rules: Rule[] = [];

  private gridApi: GridApi;

  private selectedProductIdControl: AbstractControl = this.formGroup.get(
    "selectedProductId"
  );
  private selectedSchemaIdControl: AbstractControl = this.formGroup.get(
    "selectedSchemaId"
  );

  private productsSubscription: Subscription;
  private selectedProductIdSubscription: Subscription;
  private selectedSchemaIdSubscription: Subscription;
  private selectedProductIdParamSubscription: Subscription;
  private selectedSchemaIdParamSubscription: Subscription;

  private productsLoading$ = this.store.select(getProductsLoading);

  constructor(
    private router: Router,
    private activeRoute: ActivatedRoute,
    private formBuilder: FormBuilder,
    private store: Store<any>,
    private localeTime: LocaleTimePipe
  ) {}

  private setupGrid(params: GridReadyEvent): void {
    this.gridApi = params.api;
  }

  private resizeRow(columnResizeEvent: ColumnResizedEvent): void {
    if (columnResizeEvent.finished) {
      this.gridApi.resetRowHeights();
    }
  }

  ngOnInit(): void {
    this.setupEnableDisableDropdowns();
    this.getAllProductConfigs();

    this.setupProductsUpdate();

    this.setupProductDropdownUpdate();
    this.setupSchemaDropdownUpdate();

    this.setupSelectedProductIdUpdate();
    this.setupSelectedSchemaIdUpdate();
  }

  private setupEnableDisableDropdowns(): void {
    this.productsLoading$.subscribe(loading => {
      if (loading) {
        this.selectedProductIdControl.disable();
        this.selectedSchemaIdControl.disable();
      } else {
        this.selectedProductIdControl.enable();
        this.selectedSchemaIdControl.enable();
      }
    });
  }

  private getAllProductConfigs(): void {
    this.store.dispatch(new ProductsActions.Get({ reducerMapKey: PRODUCTS }));
  }

  private setupProductsUpdate(): void {
    this.productsSubscription = this.store
      .select(getProductsCollection)
      .pipe(filter((products: Product[]) => products.length > 0))
      .subscribe((products: Product[]) => (this.products = products));
  }

  private setupProductDropdownUpdate(): void {
    this.selectedProductIdParamSubscription = combineLatest([
      this.productsLoading$,
      this.activeRoute.queryParamMap
    ])
      .pipe(
        filter(
          ([loading, queryParamMap]) =>
            !loading && queryParamMap.has("productId")
        ),
        map(([loading, queryParamMap]) =>
          parseInt(queryParamMap.get("productId"), 10)
        )
      )
      .subscribe(productId =>
        this.selectedProductIdControl.setValue(productId)
      );
  }

  private setupSchemaDropdownUpdate(): void {
    this.selectedSchemaIdParamSubscription = combineLatest([
      this.productsLoading$,
      this.activeRoute.queryParamMap
    ])
      .pipe(
        filter(
          ([loading, queryParamMap]) =>
            !loading && queryParamMap.has("schemaId")
        ),
        map(([loading, queryParamMap]) =>
          parseInt(queryParamMap.get("schemaId"), 10)
        )
      )
      .subscribe(schemaId => this.selectedSchemaIdControl.setValue(schemaId));
  }

  private setupSelectedProductIdUpdate(): void {
    this.selectedProductIdSubscription = this.selectedProductIdControl.valueChanges.subscribe(
      productId => {
        const selectedProduct = this.products.find(
          product => product.id === productId
        );
        this.selectedProductSchemas = [];

        if (selectedProduct) {
          this.selectedProductSchemas = selectedProduct.schemas;
          this.router.navigate(["rules"], {
            queryParams: { productId },
            queryParamsHandling: "merge"
          });
        }
      }
    );
  }

  private setupSelectedSchemaIdUpdate(): void {
    this.selectedSchemaIdSubscription = this.selectedSchemaIdControl.valueChanges.subscribe(
      schemaId => {
        const selectedSchema = this.selectedProductSchemas.find(
          schema => schema.id === schemaId
        );
        this.rules = [];

        if (selectedSchema) {
          this.rules = selectedSchema.validationRules;
          this.router.navigate(["rules"], {
            queryParams: { schemaId },
            queryParamsHandling: "merge"
          });
        }
      }
    );
  }

  ngOnDestroy(): void {
    this.productsSubscription.unsubscribe();

    this.selectedProductIdSubscription.unsubscribe();
    this.selectedSchemaIdSubscription.unsubscribe();

    this.selectedProductIdParamSubscription.unsubscribe();
    this.selectedSchemaIdParamSubscription.unsubscribe();
  }
}
