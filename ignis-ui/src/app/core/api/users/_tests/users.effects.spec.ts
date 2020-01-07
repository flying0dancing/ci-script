import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { provideMockActions } from "@ngrx/effects/testing";
import { StoreModule } from "@ngrx/store";
import * as deepFreeze from "deep-freeze";
import { of, ReplaySubject, throwError } from "rxjs";

import { AuthService } from "../../auth/auth.service";
import * as UsersActions from "../users.actions";
import { NAMESPACE } from "../users.constants";
import { UsersEffects } from "../users.effects";
import { reducer } from "../users.reducer";
import { UsersService } from "../users.service";

import {
  getUsersResponse,
  newPassword,
  oldPassword,
  user,
  username
} from "./users.mocks";

describe("UsersEffects", () => {
  const reducerMapKey = "test";

  let effects: UsersEffects;
  let actions: ReplaySubject<any>;
  let usersService: UsersService;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        StoreModule.forRoot({
          [NAMESPACE]: reducer
        })
      ],
      providers: [
        UsersEffects,
        UsersService,
        AuthService,
        provideMockActions(() => actions)
      ]
    });

    actions = new ReplaySubject();

    effects = TestBed.get(UsersEffects);
    usersService = TestBed.get(UsersService);
    authService = TestBed.get(AuthService);
  });

  describe("getUsers$ effect", () => {
    it("should call GET_SUCCESS action", () => {
      spyOn(usersService, "getUsers").and.returnValue(of(getUsersResponse));

      const action = new UsersActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getUsers$.subscribe(result => {
        expect(result).toEqual(
          new UsersActions.GetSuccess({
            reducerMapKey,
            collection: (getUsersResponse as any)._embedded.secUserDtoList
          })
        );
      });
    });

    it("should call GET_FAIL action", () => {
      spyOn(usersService, "getUsers").and.returnValue(throwError(undefined));

      const action = new UsersActions.Get({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getUsers$.subscribe(result => {
        expect(result).toEqual(new UsersActions.GetFail({ reducerMapKey }));
      });
    });

    it("should not call GET_SUCCESS action when EMPTY action is fired", () => {
      spyOn(usersService, "getUsers").and.returnValue(of("users"));

      const action1 = new UsersActions.Get({ reducerMapKey });
      const action2 = new UsersActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.getUsers$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("getUserByName$ effect", () => {
    it("should call GET_CURRENT_USER_SUCCESS action", () => {
      spyOn(usersService, "getUserByName").and.returnValue(of(user));

      const action = new UsersActions.GetCurrentUser({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getUserByName$.subscribe(result => {
        expect(result).toEqual(
          new UsersActions.GetCurrentUserSuccess({
            reducerMapKey,
            currentUser: user
          })
        );
      });
    });

    it("should call GET_CURRENT_USER_FAIL action", () => {
      spyOn(usersService, "getUserByName").and.returnValue(
        throwError(undefined)
      );

      const action = new UsersActions.GetCurrentUser({ reducerMapKey });

      deepFreeze(action);

      actions.next(action);

      effects.getUserByName$.subscribe(result => {
        expect(result).toEqual(
          new UsersActions.GetCurrentUserFail({ reducerMapKey })
        );
      });
    });

    it("should not call GET_CURRENT_USER_SUCCESS action when EMPTY action is fired", () => {
      spyOn(usersService, "getUserByName").and.returnValue(of({}));

      const action1 = new UsersActions.GetCurrentUser({ reducerMapKey });
      const action2 = new UsersActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.getUserByName$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("createUser$ effect", () => {
    it("should call POST_SUCCESS action", () => {
      spyOn(authService, "createUser").and.returnValue(of(user));

      const action = new UsersActions.Post({
        reducerMapKey,
        password: newPassword,
        username
      });

      deepFreeze(action);

      actions.next(action);

      effects.createUser$.subscribe(result => {
        expect(result).toEqual(new UsersActions.PostSuccess({ reducerMapKey }));
      });
    });

    it("should call POST_FAIL action", () => {
      spyOn(authService, "createUser").and.returnValue(throwError(undefined));

      const action = new UsersActions.Post({
        reducerMapKey,
        password: newPassword,
        username
      });

      deepFreeze(action);

      actions.next(action);

      effects.createUser$.subscribe(result => {
        expect(result).toEqual(new UsersActions.PostFail({ reducerMapKey }));
      });
    });

    it("should not call POST_SUCCESS action when EMPTY action is fired", () => {
      spyOn(authService, "createUser").and.returnValue(of({}));

      const action1 = new UsersActions.Post({
        reducerMapKey,
        password: newPassword,
        username
      });
      const action2 = new UsersActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.createUser$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });

  describe("changePassword$ effect", () => {
    it("should call CHANGE_PASSWORD_SUCCESS action", () => {
      spyOn(usersService, "changePassword").and.returnValue(of(user));

      const action = new UsersActions.ChangePassword({
        reducerMapKey,
        newPassword,
        oldPassword
      });

      deepFreeze(action);

      actions.next(action);

      effects.changePassword$.subscribe(result => {
        expect(result).toEqual(
          new UsersActions.ChangePasswordSuccess({ reducerMapKey })
        );
      });
    });

    it("should call CHANGE_PASSWORD_FAIL action", () => {
      spyOn(usersService, "changePassword").and.returnValue(throwError(401));

      const action = new UsersActions.ChangePassword({
        reducerMapKey,
        newPassword,
        oldPassword
      });

      deepFreeze(action);

      actions.next(action);

      effects.changePassword$.subscribe(result => {
        expect(result).toEqual(
          new UsersActions.ChangePasswordFail({
            reducerMapKey,
            statusCode: 401
          })
        );
      });
    });

    it("should not call CHANGE_PASSWORD_SUCCESS action when EMPTY action is fired", () => {
      spyOn(usersService, "changePassword").and.returnValue(of({}));

      const action1 = new UsersActions.ChangePassword({
        reducerMapKey,
        newPassword,
        oldPassword
      });
      const action2 = new UsersActions.Empty({ reducerMapKey });

      deepFreeze(action1);
      deepFreeze(action2);

      actions.next(action1);
      actions.next(action2);

      const result = [];

      effects.changePassword$.subscribe(_result => result.push(_result));

      expect(result).toEqual([]);
    });
  });
});
