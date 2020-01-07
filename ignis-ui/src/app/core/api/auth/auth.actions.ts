import { Action } from "@ngrx/store";

export const LOGIN = "auth/login";
export const LOGIN_SUCCESS = "auth/login-success";
export const LOGIN_FAIL = "auth/login-fail";
export const LOGOUT = "auth/logout";
export const LOGOUT_SUCCESS = "auth/logout-success";
export const LOGOUT_FAIL = "auth/logout-fail";
export const EMPTY = "auth/empty";

export class Login implements Action {
  readonly type = LOGIN;

  constructor(
    public payload: {
      reducerMapKey: string;
      username: string;
      password: string;
      refererUrl: string;
    }
  ) {}
}

export class LoginSuccess implements Action {
  readonly type = LOGIN_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class LoginFail implements Action {
  readonly type = LOGIN_FAIL;

  constructor(public payload: { reducerMapKey: string; error: any }) {}
}

export class Logout implements Action {
  readonly type = LOGOUT;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class LogoutSuccess implements Action {
  readonly type = LOGOUT_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class LogoutFail implements Action {
  readonly type = LOGOUT_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types =
  | Login
  | LoginSuccess
  | LoginFail
  | Logout
  | LogoutSuccess
  | LogoutFail
  | Empty;
