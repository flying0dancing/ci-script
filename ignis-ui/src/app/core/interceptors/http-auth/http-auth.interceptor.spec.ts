import { DialogHelpers } from "@/test-helpers";
import { HTTP_INTERCEPTORS, HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA, Type } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { MatDialog } from "@angular/material/dialog";
import { RouterTestingModule } from "@angular/router/testing";

import { HttpAuthDialogComponent } from "./http-auth-dialog.component";
import { HttpAuthInterceptor } from "./http-auth.interceptor";

describe("HttpAuthInterceptor", () => {
  const mockWindow = { location: { href: undefined } };

  let httpMock: HttpTestingController;
  let http: HttpClient;
  let dialog: MatDialog;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        DialogHelpers.createDialogTestingModule({
          declarations: [HttpAuthDialogComponent],
          entryComponents: [HttpAuthDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        }),
        HttpClientTestingModule
      ],
      providers: [
        { provide: "Window", useValue: mockWindow },
        {
          provide: HTTP_INTERCEPTORS,
          useClass: HttpAuthInterceptor,
          multi: true
        }
      ]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    dialog = TestBed.get(MatDialog);

    spyOn(dialog, "open").and.callThrough();
  });

  it("should open the http-auth dialog when the error response status is 401", () => {
    http.get("/data").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), {
      status: 401,
      statusText: "Not HttpAuthenticated"
    });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });

  it("should open the login dialog when the error response status is 401 and the request is a POST", () => {
    http.post("/data", {}).subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), {
      status: 401,
      statusText: "Not HttpAuthenticated"
    });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });

  it("should not open the http-auth dialog when the error response status is not 401", () => {
    http.get("/data").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), { status: 404, statusText: "Not Found" });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(0);
  });

  it("should only open the http-auth dialog once when the dialog is already open", () => {
    http.get("/data").subscribe(resp => {}, err => {});
    http.get("/data2").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), {
      status: 401,
      statusText: "Not HttpAuthenticated"
    });

    const req2 = httpMock.expectOne("/data2");
    req2.error(new ErrorEvent(""), {
      status: 401,
      statusText: "Not HttpAuthenticated"
    });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });
});
