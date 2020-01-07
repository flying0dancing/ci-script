import { DagModule } from "@/core/dag/dag.module";
import { GridModule } from "@/fcr/dashboard/shared/grid";
import { DrillbackStepContainerComponent } from "@/fcr/drillback/components/drillback-step-container.component";
import { DrillBackComponent } from "@/fcr/drillback/components/drillback.component";
import { DrillbackDatasetGridComponent } from "@/fcr/drillback/components/grid/drillback-dataset-grid.component";
import { DrillbackStepDatasetsContainerComponent } from "@/fcr/drillback/components/grid/drillback-step-datasets-container.component";
import { DrillbackMenuComponent } from "@/fcr/drillback/components/menu/drillback-menu.component";
import { DrillbackHighlightService } from "@/fcr/drillback/drillback-highlight.service";
import { DrillbackService } from "@/fcr/drillback/drillback.service";
import { PipelineSelectService } from "@/fcr/drillback/pipeline-select.service";
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';
import { LayoutModule } from "@/fcr/shared/layout/layout.module";
import { LoadersModule } from "@/shared/loaders/loaders.module";
import { DragDropModule } from "@angular/cdk/drag-drop";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatCheckboxModule } from "@angular/material";
import { MatButtonModule } from "@angular/material/button";
import { MatCardModule } from "@angular/material/card";
import { MatOptionModule } from "@angular/material/core";
import { MatDatepickerModule } from "@angular/material/datepicker";
import { MatExpansionModule } from "@angular/material/expansion";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatIconModule } from "@angular/material/icon";
import { MatInputModule } from "@angular/material/input";
import { MatSelectModule } from "@angular/material/select";
import { MatStepperModule } from "@angular/material/stepper";
import { MatTabsModule } from "@angular/material/tabs";
import { MatTooltipModule } from "@angular/material/tooltip";

@NgModule({
  imports: [
    CommonModule,
    GridModule,
    FormsModule,
    LayoutModule,
    LoadersModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatExpansionModule,
    MatIconModule,
    MatFormFieldModule,
    MatOptionModule,
    MatSelectModule,
    MatTabsModule,
    MatTooltipModule,
    MatDatepickerModule,
    MatStepperModule,
    ReactiveFormsModule,
    DragDropModule,
    DagModule,
    MatCheckboxModule,
    DateTimeModule
  ],
  declarations: [
    DrillBackComponent,
    DrillbackDatasetGridComponent,
    DrillbackStepContainerComponent,
    DrillbackStepDatasetsContainerComponent,
    DrillbackMenuComponent
  ],
  providers: [
    DrillbackService,
    PipelineSelectService,
    DrillbackHighlightService,
  ]
})
export class DrillbackModule {}
