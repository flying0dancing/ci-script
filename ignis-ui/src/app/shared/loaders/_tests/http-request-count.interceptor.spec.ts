import { Type } from "@angular/core";
import { HTTP_INTERCEPTORS, HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Store, StoreModule } from "@ngrx/store";
import * as HttpRequestCountActions from "../http-request-count.actions";

import { HttpRequestCountInterceptor } from "../http-request-count.interceptor";

describe("HttpRequestCountInterceptor", () => {
  let httpMock;
  let http;
  let store;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, StoreModule.forRoot({})],
      providers: [
        {
          provide: HTTP_INTERCEPTORS,
          useClass: HttpRequestCountInterceptor,
          multi: true
        }
      ]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    store = TestBed.get(Store);

    spyOn(store, "dispatch").and.callThrough();
  });

  it("should trigger increment and decrement store actions", () => {
    http.get("/data").subscribe();

    const req = httpMock.expectOne("/data");

    req.flush({});

    expect(store.dispatch).toHaveBeenCalledTimes(2);
    expect(store.dispatch).toHaveBeenCalledWith(
      new HttpRequestCountActions.Increment()
    );
    expect(store.dispatch).toHaveBeenCalledWith(
      new HttpRequestCountActions.Decrement()
    );

    httpMock.verify();
  });

  it("should trigger increment and decrement store actions during a network failure", () => {
    http.get("/data").subscribe(res => {}, err => {});

    const req = httpMock.expectOne("/data");

    req.error();

    expect(store.dispatch).toHaveBeenCalledTimes(2);
    expect(store.dispatch).toHaveBeenCalledWith(
      new HttpRequestCountActions.Increment()
    );
    expect(store.dispatch).toHaveBeenCalledWith(
      new HttpRequestCountActions.Decrement()
    );

    httpMock.verify();
  });
});
