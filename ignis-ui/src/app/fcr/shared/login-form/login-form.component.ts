import * as AuthActions from '@/core/api/auth/auth.actions';
import { NAMESPACE } from '@/core/api/auth/auth.constants';
import * as AuthSelectors from '@/core/api/auth/auth.selectors';
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { filter, map, pairwise } from 'rxjs/operators';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.scss']
})
export class LoginFormComponent implements OnDestroy, OnInit {
  @Input() redirect;

  private loginState$: Observable<any> = this.store.select(
    AuthSelectors.getAuthLoginState
  );
  public loginLoadingState$: Observable<any> = this.store.select(
    AuthSelectors.getAuthLoginLoadingState
  );
  public loginErrorState$: Observable<any> = this.store.select(
    AuthSelectors.getAuthLoginErrorState
  );
  private successfulLogin$ = this.loginState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev.loading && !curr.loading && !curr.error)
  );
  private loginStateSubscription: Subscription;
  public loginForm: FormGroup = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  private refererUrl: string = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private activeRoute: ActivatedRoute,
    private store: Store<any>
  ) {}

  private registerLoginSubscriptions() {
    this.loginStateSubscription = this.successfulLogin$.subscribe(
      this.handleSucessfulLogin.bind(this)
    );
  }

  private findQueryParam(paramName: string): Observable<string> {
    return this.activeRoute.queryParamMap.pipe(
      filter(queryParamMap => queryParamMap.has(paramName)),
      map(queryParamMap => queryParamMap.get(paramName))
    );
  }

  private handleSucessfulLogin() {
    this.router.navigate([this.redirect]);
  }

  public onSubmit() {
    const { username, password } = this.loginForm.getRawValue();
    this.store.dispatch(
      new AuthActions.Login({ reducerMapKey: NAMESPACE, username, password, refererUrl: this.refererUrl })
    );
  }

  ngOnInit() {
    if (this.redirect) {
      this.registerLoginSubscriptions();
    }

    this.findQueryParam('redirect')
      .pipe(
        filter(url => !!url),
        map(decodeURIComponent))
      .subscribe(url => this.refererUrl = url);
  }

  ngOnDestroy() {
    if (this.loginStateSubscription) {
      this.loginStateSubscription.unsubscribe();
    }
  }
}
