import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

import { NAMESPACE } from "./staging.constants";
import { StagingEffects } from "./staging.effects";
import { reducer } from "./staging.reducer";
import { StagingService } from "./staging.service";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([StagingEffects])
  ]
})
export class StagingModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: StagingModule,
      providers: [StagingService]
    };
  }
}
