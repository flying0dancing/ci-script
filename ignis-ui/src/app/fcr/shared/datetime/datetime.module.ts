import { LocaleDatePipe } from '@/fcr/shared/datetime/locale-date-pipe.component';
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { NgModule } from '@angular/core';

@NgModule({
  exports: [LocaleTimePipe, LocaleDatePipe],
  declarations: [LocaleTimePipe, LocaleDatePipe]
})
export class DateTimeModule {}
