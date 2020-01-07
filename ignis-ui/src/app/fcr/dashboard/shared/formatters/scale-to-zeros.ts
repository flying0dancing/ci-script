import { stringNumberWithCommas } from "./string-number-with-commas";

export const scaleToZeros = (scale: number): string =>
  `${stringNumberWithCommas(Array(Math.abs(scale) + 1).join("0"))}'s`;
