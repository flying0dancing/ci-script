import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';
import { LicenseManager } from 'ag-grid-enterprise';

import { AppModule } from './app/app.module';
import { ENVIRONMENT } from './environments/environment';

if (ENVIRONMENT.production) {
  enableProdMode();
}

LicenseManager.setLicenseKey(
  'Lombard_Risk_MultiApp_5Devs_10OEM_5_March_2019__MTU1MTc0NDAwMDAwMA==9c2c790d936542f088b0ef77c1a657ec'
);

platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .catch(err => console.log(err));
