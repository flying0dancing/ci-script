import { AuthConstants, AuthReducer } from "@/core/api/auth/";
import { DatasetsConstants, DatasetsReducer } from "@/core/api/datasets/";
import * as Features from "@/core/api/features/features.reducer";
import * as Products from "@/core/api/products/products.reducer";
import { StagingConstants, StagingReducer } from "@/core/api/staging/";
import { TablesConstants, TablesReducer } from "@/core/api/tables/";
import { UsersConstants, UsersReducer } from "@/core/api/users/";

export const reducers = {
  [DatasetsConstants.NAMESPACE]: DatasetsReducer.reducer,
  [StagingConstants.NAMESPACE]: StagingReducer.reducer,
  [TablesConstants.NAMESPACE]: TablesReducer.reducer,
  [UsersConstants.NAMESPACE]: UsersReducer.reducer,
  [AuthConstants.NAMESPACE]: AuthReducer.reducer,
  [Features.NAMESPACE]: Features.reducer,
  [Products.NAMESPACE]: Products.reducer
};
