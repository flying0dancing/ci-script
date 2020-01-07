import { PipelinesEffects } from "@/core/api/pipelines/pipelines.effects";
import { PipelinesService } from "@/core/api/pipelines/pipelines.service";
import { NAMESPACE, reducer } from "@/core/api/pipelines/pipelines.reducer";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([PipelinesEffects])
  ]
})
export class PipelinesModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: PipelinesModule,
      providers: [PipelinesService]
    };
  }
}
