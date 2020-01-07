import { NAMESPACE } from "@/core/api/auth/auth.constants";
import { AuthEffects } from "@/core/api/auth/auth.effects";
import { reducer } from "@/core/api/auth/auth.reducer";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatInputModule } from "@angular/material/input";
import { EffectsModule } from "@ngrx/effects";
import { StoreModule } from "@ngrx/store";
import { LoginFormComponent } from "./login-form.component";

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([AuthEffects])
  ],
  exports: [LoginFormComponent],
  declarations: [LoginFormComponent]
})
export class LoginFormModule {}
