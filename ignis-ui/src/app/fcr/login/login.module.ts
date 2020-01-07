import { LoginFormModule } from "@/fcr/shared/login-form";
import { NgModule } from "@angular/core";
import { MatCardModule } from "@angular/material/card";

import { LoginRoutingModule } from "./login-routing.module";
import { LoginComponent } from "./login.component";

@NgModule({
  imports: [LoginRoutingModule, LoginFormModule, MatCardModule],
  declarations: [LoginComponent]
})
export class LoginModule {}
