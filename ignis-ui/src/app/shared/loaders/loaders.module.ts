import { CommonModule } from "@angular/common";
import { HTTP_INTERCEPTORS } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { MatProgressBarModule } from "@angular/material/progress-bar";
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { StoreModule } from "@ngrx/store";

import { HttpRequestCountInterceptor } from "./http-request-count.interceptor";
import { ProgressBarComponent } from "./progress-bar.component";
import { reducers } from "./reducers";
import { SpinnerComponent } from "./spinner.component";

@NgModule({
  imports: [
    CommonModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    StoreModule.forFeature("loaders", reducers)
  ],
  declarations: [ProgressBarComponent, SpinnerComponent],
  exports: [ProgressBarComponent, SpinnerComponent],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpRequestCountInterceptor,
      multi: true
    }
  ]
})
export class LoadersModule {}
