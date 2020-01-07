import { LoginDialogComponent } from "@/fcr/shared/login-dialog/login-dialog.component";
import { LoginDialogModule } from "@/fcr/shared/login-dialog/login-dialog.module";
import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { CommonModule } from "@angular/common";
import { HTTP_INTERCEPTORS } from "@angular/common/http";
import { NgModule } from "@angular/core";
import { MatButtonModule } from "@angular/material/button";
import { MatDialogModule } from "@angular/material/dialog";
import { HttpAuthDialogComponent } from "./http-auth-dialog.component";

import { HttpAuthInterceptor } from "./http-auth.interceptor";

@NgModule({
  imports: [
    CommonModule,
    MatButtonModule,
    MatDialogModule,
    LayoutModule,
    LoginDialogModule
  ],
  providers: [
    { provide: "Window", useValue: window },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpAuthInterceptor,
      multi: true
    }
  ],
  declarations: [HttpAuthDialogComponent],
  entryComponents: [HttpAuthDialogComponent, LoginDialogComponent]
})
export class HttpAuthModule {}
