import * as HttpRequestCount from "../http-request-count.actions";
import { reducer } from "../http-request-count.reducer";

describe("httpRequestCountReducer", () => {
  it("should not affect the state on unknown action", () => {
    const initialState = 0;
    const expected = 0;
    const reducerNoTypeCheck: any = reducer;
    const actual = reducerNoTypeCheck(initialState, { type: "unknown-action" });

    expect(actual).toEqual(expected);
  });

  it("should return the initial state when no state is passed to reducer", () => {
    const initialState = undefined;
    const expected = 0;
    const reducerNoTypeCheck: any = reducer;
    const actual = reducerNoTypeCheck(initialState, { type: "unknown-action" });

    expect(actual).toEqual(expected);
  });

  it("should decrement the count state on decrement action", () => {
    const initialState = 1;
    const expected = 0;
    const actual = reducer(initialState, new HttpRequestCount.Decrement());

    expect(actual).toEqual(expected);
  });

  it("should increment the count state on increment action", () => {
    const initialState = 0;
    const expected = 1;
    const actual = reducer(initialState, new HttpRequestCount.Increment());

    expect(actual).toEqual(expected);
  });
});
