import { products } from "@/core/api/products/_tests/products.mocks";
import * as ProductActions from "@/core/api/products/products.actions";
import { ProductsEffects } from "@/core/api/products/products.effects";
import { ProductsService } from "@/core/api/products/products.service";
import { NAMESPACE } from "@/core/api/staging/staging.constants";
import { reducer } from "@/core/api/staging/staging.reducer";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { provideMockActions } from "@ngrx/effects/testing";
import { StoreModule } from "@ngrx/store";
import * as deepFreeze from "deep-freeze";
import { of, ReplaySubject, throwError } from "rxjs";

describe("ProductsEffects", () => {
  const reducerMapKey = "test";

  let effects: ProductsEffects;
  let actions: ReplaySubject<any>;
  let service: ProductsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        StoreModule.forRoot({
          [NAMESPACE]: reducer
        })
      ],
      providers: [
        ProductsEffects,
        ProductsService,
        provideMockActions(() => actions)
      ]
    });

    actions = new ReplaySubject();

    effects = TestBed.get(ProductsEffects);
    service = TestBed.get(ProductsService);
  });

  describe("get$ effect", () => {
    it("should call GET_SUCCESS action", () => {
      spyOn(service, "get").and.returnValue(of(products));

      const action = new ProductActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.get$.subscribe(result => {
        expect(result).toEqual(
          new ProductActions.GetSuccess({ reducerMapKey, products })
        );
      });
    });

    it("should call GET_FAIL action", () => {
      spyOn(service, "get").and.returnValue(throwError(undefined));

      const action = new ProductActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      let getFail;
      effects.get$.subscribe(result => (getFail = result));
      expect(getFail).toEqual(new ProductActions.GetFail({ reducerMapKey }));
    });

    it("should not return GET_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "get").and.returnValue(of("product"));

      const action1 = new ProductActions.Get({ reducerMapKey });
      const action2 = new ProductActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.get$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("delete$ effect", () => {
    it("should call DELETE_SUCCESS action", () => {
      spyOn(service, "delete").and.returnValue(of(123));

      const action = new ProductActions.Delete({ reducerMapKey, id: 123 });

      deepFreeze(action);

      actions.next(action);

      const returnedActions = [];
      effects.delete$.subscribe(result => returnedActions.push(result));

      expect(returnedActions.length).toEqual(2);
      expect(returnedActions).toContainEqual(
        new ProductActions.DeleteSuccess({ reducerMapKey, id: 123 })
      );
      expect(returnedActions).toContainEqual(
        new ProductActions.Get({ reducerMapKey })
      );
    });

    it("should call DELETE_FAIL action", () => {
      spyOn(service, "delete").and.returnValue(throwError(undefined));

      const action = new ProductActions.Delete({ reducerMapKey, id: 123 });

      deepFreeze(action);

      actions.next(action);

      let deleteFail;
      effects.delete$.subscribe(result => (deleteFail = result));

      expect(deleteFail).toEqual(
        new ProductActions.DeleteFail({ reducerMapKey })
      );
    });
  });
});
