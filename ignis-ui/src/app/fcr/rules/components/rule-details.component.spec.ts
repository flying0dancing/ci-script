import { product, products } from "@/core/api/products/_tests/products.mocks";
import * as ProductsActions from "@/core/api/products/products.actions";
import { Product } from "@/core/api/products/products.interfaces";
import { NAMESPACE as PRODUCTS } from "@/core/api/products/products.reducer";
import { table, validationRule } from "@/core/api/tables/_tests/tables.mocks";
import { Table } from "@/core/api/tables/tables.interfaces";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import * as populated from "@/fcr/dashboard/_tests/test.fixtures";
import { RuleDetailsComponent } from "@/fcr/rules/components/rule-details.component";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { MatSelectModule } from "@angular/material/select";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute, convertToParamMap, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { Store, StoreModule } from "@ngrx/store";
import { ColumnResizedEvent } from "ag-grid";
import { noop, of } from "rxjs";

describe("RuleDetailsComponent", () => {
  let ruleDetailsComponent: RuleDetailsComponent;
  let fixture: ComponentFixture<RuleDetailsComponent>;
  let store: Store<any>;
  let router: Router;
  let activatedRoute: ActivatedRoute;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatSelectModule,
        NoopAnimationsModule,
        StoreModule.forRoot(reducers),
        ReactiveFormsModule,
        RouterTestingModule
      ],
      declarations: [RuleDetailsComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleDetailsComponent);
    ruleDetailsComponent = fixture.componentInstance;
    fixture.detectChanges();
    store = TestBed.get(Store);
    router = TestBed.get(Router);
    activatedRoute = TestBed.get(ActivatedRoute);
  });

  describe("ag-Grid grid options", () => {
    it("should resize rows when column is resized", () => {
      const gridReadyEvent = { ...populated.gridReadyEvent };
      spyOn(gridReadyEvent.api, "resetRowHeights").and.callThrough();
      ruleDetailsComponent.gridOptions.onGridReady(gridReadyEvent);

      const event: ColumnResizedEvent = {
        ...populated.columnResizedEvent,
        finished: true
      };
      ruleDetailsComponent.gridOptions.onColumnResized(event);

      expect(gridReadyEvent.api.resetRowHeights).toHaveBeenCalledTimes(1);
    });
  });

  describe("ag-Grid column definitions", () => {
    it("should contain regulator reference column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "ruleId"
        )
      ).toBeTruthy();
    });

    it("should contain name column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(column => column.field === "name")
      ).toBeTruthy();
    });

    it("should contain version column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "version"
        )
      ).toBeTruthy();
    });

    it("should contain type column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "validationRuleType"
        )
      ).toBeTruthy();
    });

    it("should contain severity column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "validationRuleSeverity"
        )
      ).toBeTruthy();
    });

    it("should contain start date column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "startDate"
        )
      ).toBeTruthy();
    });

    it("should contain formatted start date column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "startDate"
        ).valueFormatter
      ).toBeTruthy();
    });

    it("should contain end date column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "endDate"
        )
      ).toBeTruthy();
    });

    it("should contain formatted end date column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "endDate"
        ).valueFormatter
      ).toBeTruthy();
    });

    it("should contain description column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "description"
        )
      ).toBeTruthy();
    });

    it("should contain expression column", () => {
      expect(
        ruleDetailsComponent.columnDefs.find(
          column => column.field === "expression"
        )
      ).toBeTruthy();
    });
  });

  describe("ngOnInit", () => {
    it("should get all products", () => {
      spyOn(store, "dispatch").and.callFake(noop);

      ruleDetailsComponent.ngOnInit();

      expect(store.dispatch).toHaveBeenCalledWith(
        new ProductsActions.Get({ reducerMapKey: PRODUCTS })
      );
    });

    it("should update products from store", () => {
      spyOn(store, "select").and.returnValue(of(products));

      ruleDetailsComponent.ngOnInit();

      expect(ruleDetailsComponent.products).toEqual(products);
    });

    it("should update productId query param when a product is selected", () => {
      spyOn(router, "navigate").and.callFake(noop);

      ruleDetailsComponent.ngOnInit();

      const selectedProduct: Product = { ...product, id: 332 };
      ruleDetailsComponent.products = [selectedProduct];
      ruleDetailsComponent.formGroup.get("selectedProductId").setValue(332);

      expect(router.navigate).toHaveBeenCalledWith(["rules"], {
        queryParams: { productId: 332 },
        queryParamsHandling: "merge"
      });
    });

    it("should update schemaId query param when a schema is selected", () => {
      spyOn(router, "navigate").and.callFake(noop);

      ruleDetailsComponent.ngOnInit();

      const selectedSchema: Table = {
        ...table,
        id: 22,
        validationRules: [validationRule]
      };
      ruleDetailsComponent.selectedProductSchemas = [selectedSchema];
      ruleDetailsComponent.formGroup.get("selectedSchemaId").setValue(22);

      expect(router.navigate).toHaveBeenCalledWith(["rules"], {
        queryParams: { schemaId: 22 },
        queryParamsHandling: "merge"
      });
    });
  });
});

describe("RuleDetailsComponent query params", () => {
  let ruleDetailsComponent: RuleDetailsComponent;
  let fixture: ComponentFixture<RuleDetailsComponent>;
  let store: Store<any>;
  let router: Router;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatSelectModule,
        NoopAnimationsModule,
        StoreModule.forRoot(reducers),
        ReactiveFormsModule,
        RouterTestingModule
      ],
      declarations: [RuleDetailsComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: of(
              convertToParamMap({
                productId: "90",
                schemaId: "68"
              })
            )
          }
        }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(RuleDetailsComponent);
    ruleDetailsComponent = fixture.componentInstance;
    fixture.detectChanges();
    store = TestBed.get(Store);
    router = TestBed.get(Router);
  });

  it("should update selected product by productId query param after products are loaded", () => {
    const productsLoadedState = of(false);
    spyOn(store, "select").and.returnValue(productsLoadedState);

    fixture = TestBed.createComponent(RuleDetailsComponent);
    ruleDetailsComponent = fixture.componentInstance;

    ruleDetailsComponent.ngOnInit();

    expect(ruleDetailsComponent.formGroup.get("selectedProductId").value).toBe(
      90
    );
  });

  it("should not update selected product by productId query param if products are not loaded", () => {
    const productsLoadingState = of(true);
    spyOn(store, "select").and.returnValue(productsLoadingState);

    fixture = TestBed.createComponent(RuleDetailsComponent);
    ruleDetailsComponent = fixture.componentInstance;

    ruleDetailsComponent.ngOnInit();

    expect(ruleDetailsComponent.formGroup.get("selectedProductId").value).toBe(
      null
    );
  });

  it("should update selected schema by schemaId query param after products are loaded", () => {
    const productsLoadedState = of(false);
    spyOn(store, "select").and.returnValue(productsLoadedState);

    fixture = TestBed.createComponent(RuleDetailsComponent);
    ruleDetailsComponent = fixture.componentInstance;

    ruleDetailsComponent.ngOnInit();

    expect(ruleDetailsComponent.formGroup.get("selectedSchemaId").value).toBe(
      68
    );
  });

  it("should not update selected schema by schemaId query param if products are not loaded", () => {
    const productsLoadingState = of(true);
    spyOn(store, "select").and.returnValue(productsLoadingState);

    fixture = TestBed.createComponent(RuleDetailsComponent);
    ruleDetailsComponent = fixture.componentInstance;

    ruleDetailsComponent.ngOnInit();

    expect(ruleDetailsComponent.formGroup.get("selectedSchemaId").value).toBe(
      null
    );
  });
});
