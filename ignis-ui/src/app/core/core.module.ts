import { DagModule } from "@/core/dag/dag.module";
import { HttpClientModule } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { ApiModule } from "./api/api.module";
import { HttpAuthModule } from "./interceptors/http-auth/http-auth.module";
import { HttpErrorModule } from "./interceptors/http-error/http-error.module";
import { StoreModule } from "./store/store.module";

@NgModule({
  imports: [
    ApiModule.forRoot(),
    HttpClientModule,
    HttpErrorModule,
    HttpAuthModule,
    StoreModule,
    DagModule
  ],
  exports: [HttpClientModule],
  declarations: []
})
export class CoreModule {}
