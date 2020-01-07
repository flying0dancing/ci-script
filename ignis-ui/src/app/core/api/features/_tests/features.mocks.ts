import { Feature } from "@/core/api/features/features.interfaces";

export const validationResultsFeature: Feature = {
  name: "VALIDATION_RULES",
  active: false
};
export const productConfigFeature: Feature = {
  name: "PRODUCT_CONFIG",
  active: true
};

export const features: Feature[] = [
  validationResultsFeature,
  productConfigFeature
];
