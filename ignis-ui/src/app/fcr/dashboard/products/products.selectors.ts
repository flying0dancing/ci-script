import * as ProductsSelectors from "@/core/api/products/products.selectors";

export const NAMESPACE = "products";

export const getProductsCollection = ProductsSelectors.getProductsCollectionFactory(
  NAMESPACE
);
export const getProductsGetState = ProductsSelectors.getProductsGetStateFactory(
  NAMESPACE
);
export const getProductsLoading = ProductsSelectors.getProductsGetLoadingStateFactory(
  NAMESPACE
);
export const importJobState = ProductsSelectors.importJobStateSelector(
  NAMESPACE
);
