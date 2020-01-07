import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { provideMockActions } from "@ngrx/effects/testing";
import { StoreModule } from "@ngrx/store";
import * as deepFreeze from "deep-freeze";
import { of, ReplaySubject, throwError } from "rxjs";

import * as DatasetsActions from "../datasets.actions";
import { NAMESPACE } from "../datasets.constants";
import { DatasetsEffects } from "../datasets.effects";
import { reducer } from "../datasets.reducer";
import { DatasetsService } from "../datasets.service";

import {
  dataset,
  datasets,
  getResponseSuccess,
  sourceFiles
} from "./datasets.mocks";

describe("DatasetsEffects", () => {
  const reducerMapKey = "test";

  let effects: DatasetsEffects;
  let actions: ReplaySubject<any>;
  let service: DatasetsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        StoreModule.forRoot({
          [NAMESPACE]: reducer
        })
      ],
      providers: [
        DatasetsEffects,
        DatasetsService,
        provideMockActions(() => actions)
      ]
    });

    actions = new ReplaySubject();

    effects = TestBed.get(DatasetsEffects);
    service = TestBed.get(DatasetsService);
  });

  describe("get$ effect", () => {
    it("should call GET_SUCCESS action", () => {
      spyOn(service, "get").and.returnValue(of(getResponseSuccess));

      const action = new DatasetsActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.get$.subscribe(result => {
        expect(result).toEqual(
          new DatasetsActions.GetSuccess({ reducerMapKey, datasets: datasets })
        );
      });
    });

    it("should call GET_FAIL action", () => {
      spyOn(service, "get").and.returnValue(throwError(undefined));

      const action = new DatasetsActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.get$.subscribe(result => {
        expect(result).toEqual(new DatasetsActions.GetFail({ reducerMapKey }));
      });
    });

    it("should not call GET_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "get").and.returnValue(of(datasets));

      const action1 = new DatasetsActions.Get({ reducerMapKey });
      const action2 = new DatasetsActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.get$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("getSourceFiles$ effect", () => {
    it("should call GET_SOURCE_FILES_SUCCESS action", () => {
      spyOn(service, "getSourceFiles").and.returnValue(of(sourceFiles));

      const action = new DatasetsActions.GetSourceFiles({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getSourceFiles$.subscribe(result => {
        expect(result).toEqual(
          new DatasetsActions.GetSourceFilesSuccess({
            reducerMapKey,
            sourceFiles: sourceFiles
          })
        );
      });
    });

    it("should call GET_SOURCE_FILES_FAIL action", () => {
      spyOn(service, "getSourceFiles").and.returnValue(throwError(undefined));

      const action = new DatasetsActions.GetSourceFiles({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getSourceFiles$.subscribe(result => {
        expect(result).toEqual(
          new DatasetsActions.GetSourceFilesFail({ reducerMapKey })
        );
      });
    });

    it("should not call GET_SOURCE_FILES_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "getSourceFiles").and.returnValue(of(dataset));

      const action1 = new DatasetsActions.GetSourceFiles({ reducerMapKey });
      const action2 = new DatasetsActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.getSourceFiles$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });
});
