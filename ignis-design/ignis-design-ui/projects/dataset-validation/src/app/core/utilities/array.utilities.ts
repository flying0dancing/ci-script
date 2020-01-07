import { FormArray } from '@angular/forms';

export const isArray = (value: any[]): boolean =>
  value && value.constructor === Array ? true : false;

export const flatten = function(arr, result = []) {
  for (let i = 0, length = arr.length; i < length; i++) {
    const value = arr[i];
    if (Array.isArray(value)) {
      flatten(value, result);
    } else {
      result.push(value);
    }
  }
  return result;
};

export function isApiErrors(errors: any): boolean {
  return (
    Array.isArray(errors) &&
    errors.length > 0 &&
    (errors[0].errorCode || errors[0].errorMessage)
  );
}

export function clearFormArray(formArray: FormArray) {
  while (formArray.length !== 0) {
    formArray.removeAt(0);
  }
}

export function max<T, ComparingValue>(
  items: T[],
  extractor: (t: T) => ComparingValue
): T {
  return items.reduce((accumulator: T, element: T) => {
    if (accumulator === undefined) {
      return element;
    }

    const extracted = extractor(element);
    const currentAccumulatorValue = extractor(accumulator);

    accumulator = extracted > currentAccumulatorValue ? element : accumulator;
    return accumulator;
  }, undefined);
}

export function min<T, ComparingValue>(
  items: T[],
  extractor: (t: T) => ComparingValue
): T {
  return items.reduce((accumulator: T, element: T) => {
    if (accumulator === undefined) {
      return element;
    }

    const extracted = extractor(element);
    const currentAccumulatorValue = extractor(accumulator);

    accumulator = extracted < currentAccumulatorValue ? element : accumulator;
    return accumulator;
  }, undefined);
}

function handleHighToLowChange<T>(values: T[], indexTo: number, indexFrom: number) {
  const itemToMove = values[indexFrom];
  const newArray: T[] = [];

  for (let index = 0; index < values.length; index++) {
    const value = values[index];

    if (index < indexTo) {
      newArray[index] = value;

    } else if (index === indexTo) {
      newArray[index] = itemToMove;

    } else if (index > indexFrom) {
      newArray[index] = value;

    } else if (index > indexTo) {
      newArray[index] = values[index - 1];
    }
  }
  return newArray;
}

function handleLowToHighChange<T>(values: T[], indexFrom: number, indexTo: number) {
  const newArray: T[] = [];
  const itemToMove = values[indexFrom];


  for (let index = 0; index < values.length; index++) {
    const value = values[index];

    if (index < indexFrom) {
      newArray[index] = value;

    } else if (index > indexTo) {
      newArray[index] = values[index];

    } else if (index === indexTo) {
      newArray[index] = itemToMove;

    } else if (index >= indexFrom) {
      newArray[index] = values[index + 1];

    }
  }
  return newArray;
}

export function moveElement<T>(indexFrom: number, indexTo: number, values: T[]): T[] {

  if (indexFrom > indexTo) {
    return handleHighToLowChange(values, indexTo, indexFrom);
  } else {
    return handleLowToHighChange(values, indexFrom, indexTo);
  }

}
