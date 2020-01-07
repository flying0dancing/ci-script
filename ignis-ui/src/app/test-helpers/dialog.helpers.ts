import { OverlayContainer } from "@angular/cdk/overlay";
import { NgModule } from "@angular/core";
import { MatDialogModule } from "@angular/material/dialog";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";

interface TestingModule extends NgModule {
  overlayContainerElement?: HTMLElement;
}

/**
 * Creates a testing module used for testing dialog components
 * See https://github.com/angular/material2/blob/3571f68a2de0833ee4741af8a0eb24d1da174f38/src/lib/dialog/dialog.spec.ts#L1021
 */
export function createDialogTestingModule(options: TestingModule) {
  const defaults = {
    imports: [],
    declarations: [],
    entryComponents: [],
    schemas: [],
    overlayContainerElement: document.createElement("div")
  };

  const newOptions = { ...defaults, ...options };

  @NgModule({
    imports: [NoopAnimationsModule, MatDialogModule, ...newOptions.imports],
    declarations: newOptions.declarations,
    entryComponents: newOptions.entryComponents,
    exports: [...newOptions.declarations, ...newOptions.entryComponents],
    providers: [
      {
        // See https://github.com/angular/material2/blob/master/src/lib/dialog/dialog.spec.ts#L43
        provide: OverlayContainer,
        useFactory: () => {
          return {
            getContainerElement: () => newOptions.overlayContainerElement
          };
        }
      }
    ],
    schemas: newOptions.schemas
  })
  class DialogTestModule {}

  return DialogTestModule;
}
