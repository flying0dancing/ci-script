import { CalendarModule } from "@/core/api/calendar/calendar.module";
import { FeaturesModule } from "@/core/api/features/features.module";
import { PipelinesModule } from "@/core/api/pipelines/pipelines.module";
import { ProductsModule } from "@/core/api/products/products.module";
import { WorkingDaysModule } from "@/core/api/working-days/working-days.module";
import { CommonModule } from "@angular/common";
import { ModuleWithProviders, NgModule } from "@angular/core";
import { AuthModule } from "./auth/auth.module";
import { DatasetsModule } from "./datasets/datasets.module";
import { StagingModule } from "./staging/staging.module";
import { TablesModule } from "./tables/tables.module";
import { UsersModule } from "./users/users.module";

@NgModule({
  imports: [
    CommonModule,
    AuthModule.forRoot(),
    FeaturesModule.forRoot(),
    CalendarModule.forRoot(),
    WorkingDaysModule.forRoot(),
    DatasetsModule.forRoot(),
    StagingModule.forRoot(),
    TablesModule.forRoot(),
    UsersModule.forRoot(),
    ProductsModule.forRoot(),
    PipelinesModule.forRoot()
  ]
})
export class ApiModule {
  static forRoot(): ModuleWithProviders {
    return {
      ngModule: ApiModule,
      providers: []
    };
  }
}
