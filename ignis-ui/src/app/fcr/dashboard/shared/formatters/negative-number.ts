export const negativeNumber = value =>
  parseFloat(value) < 0 ? `(${value.toString().substr(1)})` : value;
