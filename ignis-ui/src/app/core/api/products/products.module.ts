import { ProductsEffects } from "@/core/api/products/products.effects";
import { NAMESPACE, reducer } from "@/core/api/products/products.reducer";
import { ProductsService } from "@/core/api/products/products.service";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";

@NgModule({
  imports: [
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([ProductsEffects])
  ]
})
export class ProductsModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: ProductsModule,
      providers: [ProductsService]
    };
  }
}
