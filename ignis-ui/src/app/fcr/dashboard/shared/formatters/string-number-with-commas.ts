export const stringNumberWithCommas = (value: string) =>
  value.replace(/\B(?=(\d{3})+(?!\d))/g, ",");
