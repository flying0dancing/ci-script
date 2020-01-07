import { DrillBackComponent } from '@/fcr/drillback/components/drillback.component';
import { ErrorComponent } from '@/fcr/error/error.component';
import { RuleDetailsComponent } from '@/fcr/rules/components/rule-details.component';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: "dashboard",
    loadChildren: () =>
      import("@/fcr/dashboard/dashboard.module").then(m => m.DashboardModule)
  },
  {
    path: "login",
    redirectTo: "login",
    pathMatch: "full"
  },
  {
    path: "rules",
    component: RuleDetailsComponent
  },
  {
    path: "drillback",
    component: DrillBackComponent
  },
  {
    path: 'error',
    component: ErrorComponent
  },
  {
    path: "**",
    pathMatch: "full",
    redirectTo: "login"
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class FcrRoutingModule {}
