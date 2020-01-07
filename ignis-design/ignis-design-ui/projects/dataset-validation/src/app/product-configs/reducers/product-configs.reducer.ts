import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { ProductActions, ProductActionTypes } from '../actions/product-configs.actions';
import { ProductConfig } from '../interfaces/product-config.interface';

export const ADAPTER: EntityAdapter<ProductConfig> = createEntityAdapter<
  ProductConfig
>();

export const INITIAL_STATE: EntityState<
  ProductConfig
> = ADAPTER.getInitialState();

export function reducer(
  state = INITIAL_STATE,
  action: ProductActions
): EntityState<ProductConfig> {
  switch (action.type) {
    case ProductActionTypes.GetAllSuccessful:
      return ADAPTER.addAll(action.payload, state);

    case ProductActionTypes.GetOneSuccess:
      return ADAPTER.upsertOne(action.payload, state);

    default:
      return state;
  }
}
