import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { EffectsModule } from "@ngrx/effects";
import { StoreRouterConnectingModule } from "@ngrx/router-store";
import { StoreModule as NgrxStoreModule } from "@ngrx/store";

import { metaReducers, reducer } from "./store.reducer";

@NgModule({
  imports: [
    CommonModule,
    NgrxStoreModule.forRoot(reducer, {
      metaReducers,
      runtimeChecks: {
        strictStateImmutability: false,
        strictActionImmutability: false,
        strictStateSerializability: false,
        strictActionSerializability: false
      }
    }),
    StoreRouterConnectingModule.forRoot(),
    EffectsModule.forRoot([])
  ],
  declarations: [],
  exports: [NgrxStoreModule, StoreRouterConnectingModule]
})
export class StoreModule {}
