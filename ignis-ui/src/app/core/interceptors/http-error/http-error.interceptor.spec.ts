import { DialogHelpers } from "@/test-helpers";
import { HTTP_INTERCEPTORS, HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA, Type } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { MatDialog } from "@angular/material/dialog";

import { HttpErrorDialogComponent } from "./http-error-dialog.component";
import { HttpErrorInterceptor } from "./http-error.interceptor";

describe("HttpErrorInterceptor", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let dialog: MatDialog;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        DialogHelpers.createDialogTestingModule({
          declarations: [HttpErrorDialogComponent],
          entryComponents: [HttpErrorDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        }),
        HttpClientTestingModule
      ],
      providers: [
        { provide: "Window", useValue: window },
        {
          provide: HTTP_INTERCEPTORS,
          useClass: HttpErrorInterceptor,
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

  it("should open the error dialog when there is a network failure", () => {
    http.get("/data").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(undefined);

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });

  it("should open the error dialog when the error response status is 504", () => {
    http.get("/data").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), {
      status: 504,
      statusText: "Gateway Timeout"
    });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });

  it("should open the error dialog when the error response status is 404", () => {
    http.get("/data").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), { status: 404, statusText: "Not Found" });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });

  it("should not open the error dialog when the error response status is not 0, 404 or 504", () => {
    http.get("/data").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(new ErrorEvent(""), {
      status: 401,
      statusText: "Authentication Required"
    });

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(0);
  });

  it("should only open the error dialog once if the dialog is already open", () => {
    http.get("/data").subscribe(resp => {}, err => {});
    http.get("/data2").subscribe(resp => {}, err => {});

    const req = httpMock.expectOne("/data");
    req.error(undefined);

    const req2 = httpMock.expectOne("/data2");
    req2.error(undefined);

    httpMock.verify();

    expect(dialog.open).toHaveBeenCalledTimes(1);
  });
});
