import { stringNumberWithCommas } from "./string-number-with-commas";

export const numberWithCommas = value => {
  if (
    !isNaN(parseFloat(value)) &&
    isFinite(value) &&
    typeof value !== "string"
  ) {
    const sections = value.toString().split(".");
    sections[0] = stringNumberWithCommas(sections[0]);
    return sections.join(".");
  } else {
    return value;
  }
};
