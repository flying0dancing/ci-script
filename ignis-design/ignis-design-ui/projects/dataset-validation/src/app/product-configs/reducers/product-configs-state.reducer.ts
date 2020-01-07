import {
  INITIAL_RESPONSE_STATE,
  successfulGetOneResponse,
  toCreateFailedState,
  toCreateSuccessState,
  toCreatingState,
  toErrorState,
  toLoadingState,
  toSuccessfulGetAllState
} from '../../core/reducers/reducers-utility.service';
import { ProductActions, ProductActionTypes } from '../actions/product-configs.actions';
import { ProductResponseState } from './index';

const INITIAL_PRODUCT_RESPONSE_STATE = {
  ...INITIAL_RESPONSE_STATE,
  validating: false,
  validated: false,
  error: false
};

export function productConfigReducer(
  state = INITIAL_PRODUCT_RESPONSE_STATE,
  action: ProductActions
): ProductResponseState {
  switch (action.type) {
    case ProductActionTypes.GetOne:
      return { ...state, ...toLoadingState(state) };
    case ProductActionTypes.GetOneSuccess:
      return { ...state, ...successfulGetOneResponse(state, action) };
    case ProductActionTypes.GetOneFail:
      return { ...state, ...toErrorState(state, action.errorResponse) };

    case ProductActionTypes.GetAll:
      return { ...state, ...toLoadingState(state) };
    case ProductActionTypes.Validate:
      return { ...state, validating: true, validated: false };
    case ProductActionTypes.Create:
      return { ...state, ...toCreatingState(state) };

    case ProductActionTypes.GetAllSuccessful:
      return { ...state, ...toSuccessfulGetAllState(state, action) };

    case ProductActionTypes.ValidateSuccess:
      return { ...state, validating: false, validated: true };
    case ProductActionTypes.CreateSuccessful:
      return { ...state, ...toCreateSuccessState(state, [action.id]) };

    case ProductActionTypes.GetAllFailed:
    case ProductActionTypes.CreateFailed:
      return { ...state, ...toCreateFailedState(state, action.errorResponse) };
    case ProductActionTypes.ValidateFail:
      return { ...state, validating: false, validated: false };

    case ProductActionTypes.ValidateCancel:
      return { ...state, validating: false, validated: false };

    default:
      return state;
  }
}
