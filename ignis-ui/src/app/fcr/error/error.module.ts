import { LayoutModule } from '@/fcr/shared/layout/layout.module';
import { NgModule } from '@angular/core';
import { MatButtonModule, MatIconModule } from '@angular/material';
import { MatCardModule } from '@angular/material/card';
import { RouterModule } from '@angular/router';
import { ErrorComponent } from './error.component';

@NgModule({
  imports: [
    MatCardModule,
    LayoutModule,
    MatIconModule,
    MatButtonModule,
    RouterModule],
  declarations: [ErrorComponent]
})
export class ErrorModule {}
