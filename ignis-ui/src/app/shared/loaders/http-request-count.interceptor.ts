import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";

import { finalize } from "rxjs/operators";
import * as HttpRequestCountActions from "./http-request-count.actions";

import { State } from "./reducers";

@Injectable()
export class HttpRequestCountInterceptor implements HttpInterceptor {
  constructor(private store: Store<State>) {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    this.store.dispatch(new HttpRequestCountActions.Increment());

    return next
      .handle(req)
      .pipe(
        finalize(() =>
          this.store.dispatch(new HttpRequestCountActions.Decrement())
        )
      );
  }
}
