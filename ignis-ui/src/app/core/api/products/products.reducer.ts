import * as ProductsActions from "./products.actions";
import { Product } from "./products.interfaces";

export const NAMESPACE = "products";

export interface ProductsState {
  collection: Product[];
  get: {
    loading: boolean;
    error: boolean;
  };

  import: {
    running: boolean;
  };
}

export interface State {
  [key: string]: ProductsState;
}

export const initialProductsState: ProductsState = {
  collection: [],
  get: {
    loading: false,
    error: false
  },
  import: {
    running: false
  }
};

export function productsReducer(
  state: ProductsState = initialProductsState,
  action: ProductsActions.Types
) {
  switch (action.type) {
    case ProductsActions.GET:
      return { ...state, get: { ...state.get, loading: true } };

    case ProductsActions.GET_SUCCESS:
      return {
        ...state,
        get: { ...state.get, loading: false },
        collection: action.payload.products
      };

    case ProductsActions.GET_FAIL:
      return { ...state, get: { loading: false, error: true } };

    case ProductsActions.IMPORT_STARTED:
      return { ...state, import: { running: true } };
    case ProductsActions.NO_IMPORT_IN_PROGRESS:
      return { ...state, import: { running: false } };
    case ProductsActions.EMPTY:
      return { ...initialProductsState };

    default:
      return state;
  }
}

export function reducer(state: State = {}, action: any) {
  switch (action.type) {
    case ProductsActions.GET:
    case ProductsActions.GET_SUCCESS:
    case ProductsActions.GET_FAIL:
    case ProductsActions.IMPORT_STARTED:
    case ProductsActions.NO_IMPORT_IN_PROGRESS:
    case ProductsActions.EMPTY:
      return {
        ...state,
        [action.payload.reducerMapKey]: productsReducer(
          state[action.payload.reducerMapKey],
          action
        )
      };
    default:
      return state;
  }
}
