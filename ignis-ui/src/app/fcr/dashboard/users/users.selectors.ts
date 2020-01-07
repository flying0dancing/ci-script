import * as UsersSelectors from "@/core/api/users/users.selectors";
import { NAMESPACE } from "./users.constants";

export const getUsersCollection = UsersSelectors.getUsersCollectionFactory(
  NAMESPACE
);

export const getUsersLoading = UsersSelectors.getUsersGetLoadingStateFactory(
  NAMESPACE
);

export const getUsersPostState = UsersSelectors.getUsersPostStateFactory(
  NAMESPACE
);
