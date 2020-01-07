import * as Actions from "./http-request-count.actions";

export type Action = Actions.Types;

export function reducer(state: number = 0, action: Action): number {
  switch (action.type) {
    case Actions.INCREMENT:
      return state + 1;
    case Actions.DECREMENT:
      return state - 1;
    default:
      return state;
  }
}
