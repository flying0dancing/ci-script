import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

import { NAMESPACE } from "./tables.constants";
import { TablesEffects } from "./tables.effects";
import { reducer } from "./tables.reducer";
import { TablesService } from "./tables.service";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([TablesEffects])
  ]
})
export class TablesModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: TablesModule,
      providers: [TablesService]
    };
  }
}
