import { GridCustomOptions } from "@/fcr/dashboard/shared/grid";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from "@angular/core/testing";
import { NoopAnimationsModule } from "@angular/platform-browser/animations";
import { GridApi } from "ag-grid";

import { GridComponent } from "../grid.component";

const populatedOptions: GridCustomOptions = {
  resizeToFit: true,
  enableExport: false,
  fileName: "",
  sheetName: ""
};

describe("GridComponent", () => {
  let component: GridComponent;
  let fixture: ComponentFixture<GridComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule],
      declarations: [GridComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GridComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should be created", () => {
    expect(component).toBeTruthy();
  });

  describe("onGridReady", () => {
    it("should set the grid api", () => {
      const gridApi = new GridApi();
      component.onGridReady({ api: gridApi });

      expect(component.gridApi).toEqual(gridApi);
    });

    it("should not resize columns to fit if not set", () => {
      const gridApi = new GridApi();
      component.agGridOptions = { api: gridApi };
      component.customOptions = {
        ...populatedOptions,
        resizeToFit: false
      };

      const spy: jasmine.Spy = spyOn(gridApi, "sizeColumnsToFit");
      component.onGridReady({ api: gridApi });

      expect(spy).toHaveBeenCalledTimes(0);
    });

    it("should resize columns to fit if set", fakeAsync(() => {
      const gridApi = new GridApi();
      component.agGridOptions = { api: gridApi };
      component.customOptions = {
        ...populatedOptions,
        resizeToFit: true
      };

      const spy: jasmine.Spy = spyOn(gridApi, "sizeColumnsToFit");
      component.onGridReady({ api: gridApi });

      tick(1500);
      expect(spy).toHaveBeenCalledTimes(1);
    }));
  });

  describe("ngOnChanges", () => {
    it("should call showLoadingOverlay if loading", fakeAsync(() => {
      const gridApi = new GridApi();
      component.gridApi = gridApi;
      component.customOptions = populatedOptions;

      const spy: jasmine.Spy = spyOn(gridApi, "showLoadingOverlay");

      component.loading = true;
      component.ngOnChanges(null);

      tick(1000);

      expect(spy).toHaveBeenCalledTimes(1);
    }));
  });
});
