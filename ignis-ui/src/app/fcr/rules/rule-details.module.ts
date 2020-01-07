import { GridModule } from "@/fcr/dashboard/shared/grid";
import { RuleDetailsComponent } from "@/fcr/rules/components/rule-details.component";
import { LayoutModule } from "@/fcr/shared/layout/layout.module";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatCardModule } from "@angular/material/card";
import { MatOptionModule } from "@angular/material/core";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatSelectModule } from "@angular/material/select";
import { MatTooltipModule } from "@angular/material/tooltip";

@NgModule({
  imports: [
    CommonModule,
    GridModule,
    FormsModule,
    LayoutModule,
    LoadersModule,
    MatCardModule,
    MatFormFieldModule,
    MatOptionModule,
    MatSelectModule,
    MatTooltipModule,
    ReactiveFormsModule
  ],
  declarations: [RuleDetailsComponent],
  providers: []
})
export class RuleDetailsModule {}
