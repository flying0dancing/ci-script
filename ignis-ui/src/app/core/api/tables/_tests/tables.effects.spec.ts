import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { provideMockActions } from "@ngrx/effects/testing";
import { StoreModule } from "@ngrx/store";
import * as deepFreeze from "deep-freeze";
import { of, ReplaySubject, throwError } from "rxjs";

import * as TablesActions from "../tables.actions";
import { NAMESPACE } from "../tables.constants";
import { TablesEffects } from "../tables.effects";
import { reducer } from "../tables.reducer";
import { TablesService } from "../tables.service";

import { getResponseSuccess, tables } from "./tables.mocks";

describe("TablesEffects", () => {
  const reducerMapKey = "test";

  let effects: TablesEffects;
  let actions: ReplaySubject<any>;
  let service: TablesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        StoreModule.forRoot({
          [NAMESPACE]: reducer
        })
      ],
      providers: [
        TablesEffects,
        TablesService,
        provideMockActions(() => actions)
      ]
    });

    actions = new ReplaySubject();

    effects = TestBed.get(TablesEffects);
    service = TestBed.get(TablesService);
  });

  describe("getTables$ effect", () => {
    it("should call GET_SUCCESS action", () => {
      spyOn(service, "getTables").and.returnValue(of(getResponseSuccess));

      const action = new TablesActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getTables$.subscribe(result => {
        expect(result).toEqual(
          new TablesActions.GetSuccess({ reducerMapKey, tables })
        );
      });
    });

    it("should call GET_FAIL action", () => {
      spyOn(service, "getTables").and.returnValue(throwError(undefined));

      const action = new TablesActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getTables$.subscribe(result => {
        expect(result).toEqual(new TablesActions.GetFail({ reducerMapKey }));
      });
    });

    it("should not call GET_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "getTables").and.returnValue(of(tables));

      const action1 = new TablesActions.Get({ reducerMapKey });
      const action2 = new TablesActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.getTables$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("delete$ effect", () => {
    it("should call DELETE_SUCCESS and GET actions", () => {
      spyOn(service, "delete").and.returnValue(of({}));

      const action = new TablesActions.Delete({ reducerMapKey, id: 1 });
      const action2 = new TablesActions.DeleteSuccess({ reducerMapKey });
      const action3 = new TablesActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      const result = [];

      effects.delete$.subscribe(_result => result.push(_result));

      expect(result).toEqual([action2, action3]);
    });

    it("should call DELETE_FAIL action", () => {
      spyOn(service, "delete").and.returnValue(throwError(undefined));

      const action = new TablesActions.Delete({ reducerMapKey, id: 1 });

      deepFreeze(action);

      actions.next(action);

      effects.delete$.subscribe(result => {
        expect(result).toEqual(new TablesActions.DeleteFail({ reducerMapKey }));
      });
    });

    it("should not call DELETE_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "delete").and.returnValue(of({}));

      const action1 = new TablesActions.Delete({ reducerMapKey, id: 1 });
      const action2 = new TablesActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.delete$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });
});
