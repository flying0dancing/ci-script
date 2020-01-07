import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { MAT_DIALOG_DATA } from "@angular/material/dialog";
import { By } from "@angular/platform-browser";

import { HttpErrorDialogComponent } from "./http-error-dialog.component";

describe("HttpErrorDialogComponent", () => {
  const mockWindow = { location: { reload: () => {} } };
  const data = {};

  let component: HttpErrorDialogComponent;
  let fixture: ComponentFixture<HttpErrorDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HttpErrorDialogComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {
          provide: "Window",
          useValue: mockWindow
        },
        {
          provide: MAT_DIALOG_DATA,
          useValue: data
        }
      ]
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HttpErrorDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should trigger the reload method when the `Reload` button is clicked", () => {
    spyOn(component, "reload").and.callThrough();

    const buttons = fixture.debugElement.queryAll(By.css("button"));
    const reloadButton = buttons[1];

    reloadButton.triggerEventHandler("click", undefined);

    fixture.detectChanges();

    expect(component.reload).toHaveBeenCalledTimes(1);
  });

  it("should update the errors if included in the data", () => {
    component["data"] = {
      correlationId: "1234",
      errors: [
        {
          errorMessage: "Error!",
          errorCode: "OOPS"
        }
      ]
    };

    component.ngOnInit();

    expect(component.errors).toEqual([
      { errorMessage: "Error!", errorCode: "OOPS" }
    ]);
  });

  it("should update the errors with 3 items in data", () => {
    component["data"] = {
      errors: [
        { errorMessage: "", errorCode: "" },
        { errorMessage: "", errorCode: "" },
        { errorMessage: "", errorCode: "" }
      ]
    };

    component.ngOnInit();

    expect(component.errors.length).toEqual(3);
  });

  it("should not update the errors with 0 items in data", () => {
    component["data"] = [];

    component.ngOnInit();

    expect(component.errors).toEqual([
      {
        errorCode: "",
        errorMessage: "We're having trouble with your request right now."
      }
    ]);
  });

  it("should not update the errors if data is not an array of errors", () => {
    component["data"] = { isTrusted: true };

    component.ngOnInit();

    expect(component.errors).toEqual([
      {
        errorCode: "",
        errorMessage: "We're having trouble with your request right now."
      }
    ]);
  });
});
