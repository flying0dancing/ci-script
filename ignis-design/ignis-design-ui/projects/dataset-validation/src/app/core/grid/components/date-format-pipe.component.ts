import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'dateFormatPipe'
})
export class DateFormatPipe implements PipeTransform {
  transform(value: string) {
    return value ? new Date(value).toLocaleDateString() : '';
  }
}
