import { NO_ERRORS_SCHEMA } from "@angular/core";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from "@angular/core/testing";

import { HttpAuthDialogComponent } from "./http-auth-dialog.component";

describe("HttpHttpAuthDialogComponent", () => {
  const mockWindow = { location: { href: undefined } };
  let component: HttpAuthDialogComponent;
  let fixture: ComponentFixture<HttpAuthDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [HttpAuthDialogComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {
          provide: "Window",
          useValue: mockWindow
        }
      ]
    });
  });

  it("should redirect after 10 seconds to the login page", fakeAsync(() => {
    fixture = TestBed.createComponent(HttpAuthDialogComponent);
    component = fixture.componentInstance;

    spyOn(component, "login").and.callThrough();

    tick(10000);

    fixture.detectChanges();

    expect(component.login).toHaveBeenCalled();
  }));
});
