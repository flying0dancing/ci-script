import { FeaturesEffects } from "@/core/api/features/features.effects";
import { NAMESPACE, reducer } from "@/core/api/features/features.reducer";
import { FeaturesService } from "@/core/api/features/features.service";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([FeaturesEffects])
  ]
})
export class FeaturesModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: FeaturesModule,
      providers: [FeaturesService]
    };
  }
}
