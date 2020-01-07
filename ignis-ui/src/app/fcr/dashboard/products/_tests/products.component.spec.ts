import { CoreModule } from "@/core";
import * as FeaturesActions from "@/core/api/features/features.actions";
import { IgnisFeature } from "@/core/api/features/features.enum";
import { NAMESPACE as FEATURE_NAMESPACE } from "@/core/api/features/features.reducer";
import * as ProductsActions from "@/core/api/products/products.actions";
import { Product } from "@/core/api/products/products.interfaces";
import { ProductsService } from "@/core/api/products/products.service";
import * as Populated from "@/core/api/tables/_tests/tables.mocks.ts";
import { Table } from "@/core/api/tables/tables.interfaces";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import {
  ProductItemGroupKey,
  ProductRowItem
} from "@/fcr/dashboard/products/product-schema.interface";
import { ProductsComponent } from "@/fcr/dashboard/products/products.component";
import { NAMESPACE } from "@/fcr/dashboard/products/products.selectors";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { RouterTestingModule } from "@angular/router/testing";
import { Store, StoreModule } from "@ngrx/store";

describe("ProductListComponent", () => {
  let component: ProductsComponent;
  let fixture: ComponentFixture<ProductsComponent>;
  let store: Store<any>;
  const reducerMapKey = NAMESPACE;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        MatSnackBarModule,
        CoreModule,
        StoreModule.forRoot(reducers)
      ],
      providers: [ProductsService],
      declarations: [ProductsComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    store = TestBed.get(Store);
    fixture = TestBed.createComponent(ProductsComponent);
    component = fixture.componentInstance;
    spyOn(store, "dispatch").and.callThrough();
    fixture.detectChanges();
  }));

  describe("ngOnInit", () => {
    it("should dispatch the empty and get products actions", () => {
      const action1 = new ProductsActions.Empty({ reducerMapKey });
      const action2 = new ProductsActions.Get({ reducerMapKey });

      component.ngOnInit();
      expect(store.dispatch).toHaveBeenCalledWith(action1);
      expect(store.dispatch).toHaveBeenCalledWith(action2);
    });
  });

  describe("refreshProducts", () => {
    it("should dispatch the get products action", () => {
      const getProductsAction = new ProductsActions.Get({ reducerMapKey });

      component.ngOnInit();
      expect(store.dispatch).toHaveBeenCalledWith(getProductsAction);
    });
  });

  describe("productsCollection Transformation Feature inactive", () => {
    it("should be empty if store is empty", () => {
      store.dispatch(
        new FeaturesActions.GetSuccess({
          reducerMapKey: FEATURE_NAMESPACE,
          features: [
            {
              name: IgnisFeature.APPEND_DATASETS,
              active: false
            }
          ]
        })
      );
      let products: ProductRowItem[];

      component.productSchemas$.subscribe(
        productSchemas => (products = productSchemas)
      );

      const productSuccess = new ProductsActions.GetSuccess({
        reducerMapKey: NAMESPACE,
        products: []
      });
      store.dispatch(productSuccess);

      expect(products).toEqual([]);
    });

    it("should contain product schemas from store", () => {
      store.dispatch(
        new FeaturesActions.GetSuccess({
          reducerMapKey: FEATURE_NAMESPACE,
          features: [
            {
              name: IgnisFeature.APPEND_DATASETS,
              active: false
            }
          ]
        })
      );

      const table1: Table = {
        id: 1,
        displayName: "table1",
        physicalTableName: "tbl1",
        version: 0,
        startDate: "2001-1-1-",
        createdTime: 123,
        hasDatasets: true,
        validationRules: []
      };
      const table2: Table = {
        id: 2,
        displayName: "table2",
        physicalTableName: "tbl2",
        version: 0,
        startDate: "2001-1-1-",
        createdTime: 0,
        hasDatasets: false,
        validationRules: []
      };
      const table3: Table = {
        id: 3,
        displayName: "table3",
        physicalTableName: "tbl3",
        version: 0,
        startDate: "2001-1-1-",
        createdTime: 0,
        hasDatasets: false,
        validationRules: []
      };
      const product1: Product = {
        id: 101,
        name: "product1",
        version: "2.0",
        schemas: [table1],
        pipelines: [],
        importStatus: "SUCCESS"
      };
      const product2: Product = {
        id: 102,
        name: "product2",
        version: "3.0",
        schemas: [table3, table2],
        pipelines: [],
        importStatus: "SUCCESS"
      };

      const productSuccess = new ProductsActions.GetSuccess({
        reducerMapKey: NAMESPACE,
        products: [product1, product2]
      });

      let products: ProductRowItem[];
      component.productSchemas$.subscribe(
        productSchemas => (products = productSchemas)
      );

      store.dispatch(productSuccess);

      expect(products).toEqual([
        {
          productId: 101,
          productName: "product1",
          productVersion: "2.0",
          group: ProductItemGroupKey.SCHEMA,
          itemId: 1,
          itemName: "table1",
          version: 0,
          startDate: "2001-1-1-",
          endDate: undefined,
          createdTime: 123,
          importStatus: "SUCCESS"
        },
        {
          productId: 102,
          productName: "product2",
          productVersion: "3.0",
          group: ProductItemGroupKey.SCHEMA,
          itemId: 3,
          itemName: "table3",
          version: 0,
          startDate: "2001-1-1-",
          endDate: undefined,
          createdTime: 0,
          importStatus: "SUCCESS"
        },
        {
          productId: 102,
          productName: "product2",
          productVersion: "3.0",
          group: ProductItemGroupKey.SCHEMA,
          itemId: 2,
          itemName: "table2",
          version: 0,
          startDate: "2001-1-1-",
          endDate: undefined,
          createdTime: 0,
          importStatus: "SUCCESS"
        }
      ]);
    });
  });

  describe("unstagedProductsCollection", () => {
    it("should be empty if there are no products in the store", () => {
      const productSuccess = new ProductsActions.GetSuccess({
        reducerMapKey: NAMESPACE,
        products: []
      });

      let unstagedProducts: Product[];
      component.unstagedProducts$.subscribe(
        products => (unstagedProducts = products)
      );

      store.dispatch(productSuccess);

      expect(unstagedProducts).toEqual([]);
    });

    it("should contain unstaged products", () => {
      const table1: Table = { ...Populated.table, hasDatasets: false, id: 1 };
      const table2: Table = { ...Populated.table, hasDatasets: true, id: 2 };
      const table3: Table = { ...Populated.table, hasDatasets: true, id: 3 };
      const product1: Product = {
        id: 101,
        name: "product1",
        version: "2.0",
        schemas: [table1],
        importStatus: "SUCCESS",
        pipelines: []
      };
      const product2: Product = {
        id: 102,
        name: "product2",
        version: "3.0",
        schemas: [table3, table2],
        importStatus: "SUCCESS",
        pipelines: []
      };
      const product3: Product = {
        id: 102,
        name: "product2",
        version: "3.0",
        schemas: [table3],
        importStatus: "SUCCESS",
        pipelines: []
      };

      const productSuccess = new ProductsActions.GetSuccess({
        reducerMapKey: NAMESPACE,
        products: [product1, product2, product3]
      });

      let unstagedProducts: Product[];
      component.unstagedProducts$.subscribe(
        products => (unstagedProducts = products)
      );

      store.dispatch(productSuccess);

      expect(unstagedProducts).toEqual([product1]);
    });
  });
});
