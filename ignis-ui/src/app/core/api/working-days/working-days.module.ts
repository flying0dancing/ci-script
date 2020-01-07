import { NAMESPACE } from "@/core/api/working-days/working-days.actions";
import { WorkingDaysEffects } from "@/core/api/working-days/working-days.effects";
import { reducer } from "@/core/api/working-days/working-days.reducer";
import { WorkingDaysService } from "@/core/api/working-days/working-days.service";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([WorkingDaysEffects])
  ]
})
export class WorkingDaysModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: WorkingDaysModule,
      providers: [WorkingDaysService]
    };
  }
}
