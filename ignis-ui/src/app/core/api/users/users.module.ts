import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

import { NAMESPACE } from "./users.constants";
import { UsersEffects } from "./users.effects";
import { reducer } from "./users.reducer";
import { UsersService } from "./users.service";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([UsersEffects])
  ]
})
export class UsersModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: UsersModule,
      providers: [UsersService]
    };
  }
}
