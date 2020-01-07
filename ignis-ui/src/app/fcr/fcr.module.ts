import { CoreModule } from '@/core';
import { JobsModule } from '@/fcr/dashboard/jobs/jobs.module';
import { ErrorModule } from '@/fcr/error';
import { FcrRoutingModule } from '@/fcr/fcr-routing.module';
import { LoginModule } from '@/fcr/login';
import { ConfirmDialogModule } from '@/shared/dialogs/confirm-dialog/confirm-dialog.module';
import { LoadersModule } from '@/shared/loaders/loaders.module';
import { NgModule } from '@angular/core';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { environment } from '@env/environment';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { FcrComponent } from './fcr.component';
import { LayoutModule } from './shared/layout/layout.module';

@NgModule({
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    CoreModule,
    FcrRoutingModule,
    MatMomentDateModule,
    LayoutModule,
    ConfirmDialogModule,
    LoadersModule,
    LoginModule,
    ErrorModule,
    JobsModule.forRoot(),
    !environment.production ? StoreDevtoolsModule.instrument() : []
  ],
  declarations: [FcrComponent],
  providers: [],
  bootstrap: [FcrComponent]
})
export class FcrModule {}
