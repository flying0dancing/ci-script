import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

import { NAMESPACE } from "./datasets.constants";
import { DatasetsEffects } from "./datasets.effects";
import { reducer } from "./datasets.reducer";
import { DatasetsService } from "./datasets.service";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([DatasetsEffects])
  ]
})
export class DatasetsModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: DatasetsModule,
      providers: [DatasetsService]
    };
  }
}
