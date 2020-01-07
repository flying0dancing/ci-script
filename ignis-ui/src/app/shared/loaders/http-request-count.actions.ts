import { Action } from "@ngrx/store";

export const INCREMENT = "loaders/http-request-count/increment";
export const DECREMENT = "loaders/http-request-count/decrement";

export class Increment implements Action {
  readonly type = INCREMENT;
}

export class Decrement implements Action {
  readonly type = DECREMENT;
}

export type Types = Increment | Decrement;
