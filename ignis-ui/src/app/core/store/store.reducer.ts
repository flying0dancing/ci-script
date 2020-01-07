import { environment } from "@env/environment";
import * as fromRouter from "@ngrx/router-store";
import { ActionReducer } from "@ngrx/store";
import { storeLogger } from "ngrx-store-logger";

export interface State {
  router: fromRouter.RouterReducerState;
}

export const reducer = {
  router: fromRouter.routerReducer
};

export function logger(actionReducer: ActionReducer<State>): any {
  // default, no options
  return storeLogger()(actionReducer);
}

export const metaReducers = environment.storeLogging ? [logger] : [];
