import { NAMESPACE } from "@/core/api/calendar/calendar.actions";
import { CalendarService } from "@/core/api/calendar/calendar.service";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

import { CalendarEffects } from "./calendar.effects";
import { reducer } from "./calendar.reducer";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([CalendarEffects])
  ]
})
export class CalendarModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: CalendarModule,
      providers: [CalendarService]
    };
  }
}
