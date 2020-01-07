import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { HttpErrorDialogComponent } from './http-error-dialog.component';

import { HttpErrorInterceptor } from './http-error.interceptor';

@NgModule({
  imports: [CommonModule, MatButtonModule, MatDialogModule],
  providers: [
    { provide: 'Window', useValue: window },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true
    }
  ],
  declarations: [HttpErrorDialogComponent],
  entryComponents: [HttpErrorDialogComponent]
})
export class HttpErrorModule {}
