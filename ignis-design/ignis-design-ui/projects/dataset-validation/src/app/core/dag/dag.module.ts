import { NgModule } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DagComponent } from './dag.component';
import { CommonModule } from '@angular/common';

@NgModule({
  imports: [CommonModule, MatTooltipModule],
  declarations: [DagComponent],
  exports: [DagComponent]
})
export class DagModule {}
