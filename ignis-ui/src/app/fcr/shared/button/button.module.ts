import { SaveButtonComponent } from "@/fcr/shared/button/save-button.component";
import { LayoutModule } from "@/shared/dialogs/layout/layout.module";
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { ReactiveFormsModule } from "@angular/forms";
import {
  MatIconModule,
  MatProgressSpinnerModule,
  MatTooltipModule
} from "@angular/material";
import { MatButtonModule } from "@angular/material/button";

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressSpinnerModule,
    LayoutModule
  ],
  exports: [SaveButtonComponent],
  declarations: [SaveButtonComponent]
})
export class ButtonModule {}
