import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'localeDate'
})
export class LocaleDatePipe implements PipeTransform {
  transform(value: any) {
    return value ? new Date(value).toLocaleDateString() : '';
  }
}
