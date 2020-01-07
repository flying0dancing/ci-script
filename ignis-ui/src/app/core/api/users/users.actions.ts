import { Action } from "@ngrx/store";
import * as Interfaces from "./users.interfaces";

export const GET = "users/get";
export const GET_SUCCESS = "users/get-success";
export const GET_FAIL = "users/get-fail";
export const GET_CURRENT_USER = "users/get-current-user";
export const GET_CURRENT_USER_SUCCESS = "users/get-current-user-success";
export const GET_CURRENT_USER_FAIL = "users/get-current-user-fail";
export const POST = "users/post";
export const POST_SUCCESS = "users/post-success";
export const POST_FAIL = "users/post-fail";
export const CHANGE_PASSWORD = "users/change-password";
export const CHANGE_PASSWORD_SUCCESS = "users/change-password-success";
export const CHANGE_PASSWORD_FAIL = "users/change-password-fail";
export const CLEAR_PASSWORD_ERRORS = "users/clear-password-errors";
export const EMPTY = "users/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; collection: Interfaces.User[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetCurrentUser implements Action {
  readonly type = GET_CURRENT_USER;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetCurrentUserSuccess implements Action {
  readonly type = GET_CURRENT_USER_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; currentUser: Interfaces.User }
  ) {}
}

export class GetCurrentUserFail implements Action {
  readonly type = GET_CURRENT_USER_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Post implements Action {
  readonly type = POST;

  constructor(
    public payload: {
      reducerMapKey: string;
      username: string;
      password: string;
    }
  ) {}
}

export class PostSuccess implements Action {
  readonly type = POST_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class PostFail implements Action {
  readonly type = POST_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class ChangePassword implements Action {
  readonly type = CHANGE_PASSWORD;

  constructor(
    public payload: {
      reducerMapKey: string;
      oldPassword: string;
      newPassword: string;
    }
  ) {}
}

export class ChangePasswordSuccess implements Action {
  readonly type = CHANGE_PASSWORD_SUCCESS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class ChangePasswordFail implements Action {
  readonly type = CHANGE_PASSWORD_FAIL;

  constructor(public payload: { reducerMapKey: string; statusCode }) {}
}

export class ClearPasswordErrors implements Action {
  readonly type = CLEAR_PASSWORD_ERRORS;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types =
  | Get
  | GetSuccess
  | GetFail
  | GetCurrentUser
  | GetCurrentUserSuccess
  | GetCurrentUserFail
  | Post
  | PostSuccess
  | PostFail
  | ChangePassword
  | ChangePasswordSuccess
  | ChangePasswordFail
  | ClearPasswordErrors
  | Empty;
