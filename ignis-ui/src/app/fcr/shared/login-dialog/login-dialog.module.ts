import { NAMESPACE } from "@/core/api/auth/auth.constants";
import { AuthEffects } from "@/core/api/auth/auth.effects";
import { reducer } from "@/core/api/auth/auth.reducer";
import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { MatDialogModule } from "@angular/material/dialog";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";
import { LoginFormModule } from "../login-form/login-form.module";
import { LoginDialogComponent } from "./login-dialog.component";

@NgModule({
  imports: [
    LoginFormModule,
    MatDialogModule,
    LayoutModule,
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([AuthEffects]),
    CommonModule
  ],
  exports: [LoginDialogComponent],
  declarations: [LoginDialogComponent]
})
export class LoginDialogModule {}
