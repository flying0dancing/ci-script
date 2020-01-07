import { Pipe, PipeTransform } from '@angular/core';

const NUMBER_REPLACER_REGEXP = /[^\d.-]/g;

@Pipe({ name: 'numbersOnlyString' })
export class NumbersOnlyStringPipe implements PipeTransform {
  transform(value: string): string {
    if (!value) {
      return null;
    } else {
      return value.replace(NUMBER_REPLACER_REGEXP, '');
    }
  }
}
