import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";

import { LoginComponent } from "./login.component";

const LR_LOGIN_ROUTES: Routes = [{ path: "login", component: LoginComponent }];

@NgModule({
  imports: [RouterModule.forChild(LR_LOGIN_ROUTES)],
  exports: [RouterModule]
})
export class LoginRoutingModule {}
