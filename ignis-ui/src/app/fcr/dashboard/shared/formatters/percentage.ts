export const percentage = value =>
  !isNaN(parseFloat(value)) ? `${parseFloat(value).toFixed(2)}%` : value;
