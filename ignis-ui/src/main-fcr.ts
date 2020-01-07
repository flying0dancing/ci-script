import { FcrModule } from "@/fcr/fcr.module";
import { enableProdMode, LOCALE_ID } from "@angular/core";
import { platformBrowserDynamic } from "@angular/platform-browser-dynamic";
import { environment } from "@env/environment";
import { LicenseManager } from "ag-grid-enterprise/main";

if (environment.production) {
  enableProdMode();
}

LicenseManager.setLicenseKey(
  "Lombard_Risk_MultiApp_5Devs_10OEM_5_March_2019__MTU1MTc0NDAwMDAwMA==9c2c790d936542f088b0ef77c1a657ec"
);

// See https://github.com/angular/angular-cli/issues/6683#issuecomment-308960646
const localeProvider = { provide: LOCALE_ID, useValue: navigator.language };

platformBrowserDynamic([localeProvider])
  .bootstrapModule(FcrModule, { providers: [localeProvider] })
  .catch(err => console.log(err));
