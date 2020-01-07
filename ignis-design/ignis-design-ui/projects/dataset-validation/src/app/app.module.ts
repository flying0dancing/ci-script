import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { GestureConfig } from '@angular/material/core';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import {
  BrowserModule,
  HAMMER_GESTURE_CONFIG
} from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { AgGridModule } from 'ag-grid-angular';
import 'ag-grid-enterprise';
import { HotkeyModule } from 'angular2-hotkeys';
import { MonacoEditorModule } from 'ngx-monaco-editor';

import { ENVIRONMENT } from '../environments/environment';
import { ENV_TOKEN } from '../environments/environment.service';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { EditorUtils } from './core/forms/editor-utils';
import { CHEAT_SHEET_SHORTCUT } from './core/shortcuts/shortcuts.service';
import { PipelineStepTestsModule } from './pipeline-step-tests/pipeline-step-tests.module';
import { ProductConfigsModule } from './product-configs/product-configs.module';
import { SchemaDetailsModule } from './schema-details/schema-details.module';
import { SchemasModule } from './schemas/schemas.module';

@NgModule({
  declarations: [AppComponent],
  imports: [
    AppRoutingModule,
    BrowserAnimationsModule,
    BrowserModule,
    CoreModule,
    HttpClientModule,
    MatMomentDateModule,
    SchemaDetailsModule,
    SchemasModule,
    ProductConfigsModule,
    PipelineStepTestsModule,
    AgGridModule.withComponents([]),
    EffectsModule.forRoot([]),
    HotkeyModule.forRoot({
      cheatSheetHotkey: CHEAT_SHEET_SHORTCUT
    }),
    MonacoEditorModule.forRoot(EditorUtils.defaultConfig),
    StoreModule.forRoot(
      {},
      {
        runtimeChecks: {
          strictStateImmutability: false,
          strictActionImmutability: false,
          strictStateSerializability: false,
          strictActionSerializability: false
        }
      }
    ),
    !ENVIRONMENT.production ? StoreDevtoolsModule.instrument() : []
  ],
  providers: [
    { provide: HAMMER_GESTURE_CONFIG, useClass: GestureConfig },
    { provide: ENV_TOKEN, useValue: ENVIRONMENT }
  ],
  bootstrap: [AppComponent]
})
export class AppModule {}
