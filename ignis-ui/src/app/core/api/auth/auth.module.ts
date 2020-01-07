import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";
import { NAMESPACE } from "./auth.constants";
import { AuthEffects } from "./auth.effects";
import { reducer } from "./auth.reducer";

import { AuthService } from "./auth.service";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([AuthEffects])
  ]
})
export class AuthModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: AuthModule,
      providers: [AuthService]
    };
  }
}
