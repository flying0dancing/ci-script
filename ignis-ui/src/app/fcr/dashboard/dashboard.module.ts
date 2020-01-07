import { UsersModule } from "@/core/api/users/users.module";
import { ProductsModule } from "@/fcr/dashboard/products/products.module";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { CommonModule, DatePipe } from "@angular/common";
import { NgModule } from "@angular/core";
import {
  MatBadgeModule,
  MatExpansionModule,
  MatIconModule,
  MatSnackBarModule
} from "@angular/material/";
import { DatasetListModule } from "../dashboard/datasets/datasets.module";
import { JobsModule } from "../dashboard/jobs/jobs.module";
import { SentenceCasePipe } from "../dashboard/shared/pipes";
import { LayoutModule as FcrLayoutModule } from "../shared/layout/layout.module";
import { DashboardRoutingModule } from "./dashboard-routing.module";
import { DashboardComponent } from "./dashboard.container";

@NgModule({
  imports: [
    CommonModule,
    MatSnackBarModule,
    MatBadgeModule,
    DashboardRoutingModule,
    FcrLayoutModule,
    LoadersModule,
    JobsModule,
    DatasetListModule,
    ProductsModule,
    MatExpansionModule,
    MatIconModule,
    UsersModule
  ],
  declarations: [DashboardComponent, SentenceCasePipe],
  providers: [DatePipe, SentenceCasePipe]
})
export class DashboardModule {}
