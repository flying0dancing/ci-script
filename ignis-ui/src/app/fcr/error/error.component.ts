import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';

@Component({
  selector: 'app-error',
  templateUrl: './error.component.html',
  styleUrls: ['./error.component.scss']
})
export class ErrorComponent implements OnInit {
  private errorParamSubscription: Subscription;
  errorMessage: string;
  errorTitle: string;

  constructor(
    private router: Router,
    private activeRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.errorTitle = 'Something went wrong';
    this.errorMessage = 'The page you are looking for might have been removed, had its name changed or is temporarily unavailable.';

    this.errorTitleQueryParamUpdate();
    this.errorMessageQueryParamUpdate();
  }

  private findQueryParam(paramName: string): Observable<string> {
    return this.activeRoute.queryParamMap.pipe(
      filter(queryParamMap => queryParamMap.has(paramName)),
      map(queryParamMap => queryParamMap.get(paramName))
    );
  }

  private errorTitleQueryParamUpdate() {
    this.errorParamSubscription = this.findQueryParam('errorTitle').subscribe(
      (errorTitle: string) =>
        (this.errorTitle = errorTitle)
    );
  }

  private errorMessageQueryParamUpdate() {
    this.errorParamSubscription = this.findQueryParam('errorMessage').subscribe(
      (errorMessage: string) =>
        (this.errorMessage = errorMessage)
    );
  }
}
