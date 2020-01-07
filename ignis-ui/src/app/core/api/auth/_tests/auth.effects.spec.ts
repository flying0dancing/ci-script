import * as UsersActions from "@/core/api/users/users.actions";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { provideMockActions } from "@ngrx/effects/testing";
import { StoreModule } from "@ngrx/store";
import * as deepFreeze from "deep-freeze";
import { noop, of, ReplaySubject, throwError } from "rxjs";
import * as AuthActions from "../auth.actions";
import { NAMESPACE } from "../auth.constants";
import { AuthEffects } from "../auth.effects";
import { reducer } from "../auth.reducer";
import { AuthService } from "../auth.service";

import { loginError, password, username } from "./auth.mocks";

describe("AuthEffects", () => {
  const reducerMapKey = "test";

  let effects: AuthEffects;
  let actions: ReplaySubject<any>;
  let service: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,

        RouterTestingModule,
        StoreModule.forRoot({
          [NAMESPACE]: reducer
        })
      ],
      providers: [AuthEffects, AuthService, provideMockActions(() => actions)]
    });

    actions = new ReplaySubject();

    effects = TestBed.get(AuthEffects);
    service = TestBed.get(AuthService);
    router = TestBed.get(Router);
  });

  describe("login$ effect", () => {
    it("should call LOGIN_SUCCESS action", () => {
      spyOn(service, "login").and.returnValue(of({}));

      const action = new AuthActions.Login({
        reducerMapKey,
        username,
        password,
        refererUrl: null
      });

      deepFreeze(action);

      actions.next(action);

      effects.login$.subscribe(result => {
        expect(result).toEqual([
          new AuthActions.LoginSuccess({ reducerMapKey }),
          new UsersActions.Empty({ reducerMapKey })
        ]);
      });
    });

    it("should redirect to login page", () => {
      spyOn(service, "login").and.returnValue(of({}));

      const action = new AuthActions.Login({
        reducerMapKey,
        username,
        password,
        refererUrl: null
      });

      deepFreeze(action);
      spyOn(router, "navigate").and.callFake(noop);

      actions.next(action);

      effects.login$.subscribe(() => {
        expect(router.navigate).toHaveBeenCalledWith(["/login"]);
      });
    });

    it("should call LOGIN_FAIL action", () => {
      spyOn(service, "login").and.returnValue(throwError(loginError));

      const action = new AuthActions.Login({
        reducerMapKey,
        username,
        password,
        refererUrl: null
      });

      deepFreeze(action);

      actions.next(action);

      effects.login$.subscribe(result => {
        expect(result).toEqual(
          new AuthActions.LoginFail({ reducerMapKey, error: loginError })
        );
      });
    });

    it("should not call LOGIN_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "login").and.returnValue(of("auth"));

      const action1 = new AuthActions.Login({
        reducerMapKey,
        username,
        password,
        refererUrl: null
      });
      const action2 = new AuthActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.login$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("logout$ effect", () => {
    it("should call LOGOUT_SUCCESS action", () => {
      spyOn(service, "logout").and.returnValue(of({}));

      const action = new AuthActions.Logout({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.logout$.subscribe(result => {
        expect(result).toEqual(
          new AuthActions.LogoutSuccess({ reducerMapKey })
        );
      });
    });

    it("should call LOGOUT_FAIL action", () => {
      spyOn(service, "logout").and.returnValue(throwError(undefined));

      const action = new AuthActions.Logout({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.logout$.subscribe(result => {
        expect(result).toEqual(new AuthActions.LogoutFail({ reducerMapKey }));
      });
    });

    it("should not call LOGOUT_SUCCESS action when EMPTY action is fired", () => {
      spyOn(service, "logout").and.returnValue(of({}));

      const action1 = new AuthActions.Logout({ reducerMapKey });
      const action2 = new AuthActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.logout$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });
});
