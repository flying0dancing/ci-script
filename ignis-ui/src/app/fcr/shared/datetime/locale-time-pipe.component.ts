import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'localeTime'
})
export class LocaleTimePipe implements PipeTransform {
  transform(value: any) {
    return value ? new Date(value).toLocaleString() : '';
  }
}
