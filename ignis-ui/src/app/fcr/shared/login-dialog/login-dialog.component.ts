import * as AuthSelectors from "@/core/api/auth/auth.selectors";
import { Component, Inject, Input, OnDestroy } from "@angular/core";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { Store } from "@ngrx/store";
import { Observable, Subscription } from "rxjs";
import { filter, pairwise } from "rxjs/operators";

@Component({
  selector: "app-login-dialog",
  templateUrl: "./login-dialog.component.html"
})
export class LoginDialogComponent implements OnDestroy {
  @Input() title = "Login";
  private loginStateSubscription: Subscription;
  private loginState$: Observable<any> = this.store.select(
    AuthSelectors.getAuthLoginState
  );
  public successfulLogin$ = this.loginState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev.loading && !curr.loading && !curr.error)
  );

  constructor(
    public dialogRef: MatDialogRef<LoginDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private store: Store<any>
  ) {
    if (data.title) {
      this.title = data.title;
    }
    this.registerLoginSubscriptions();
  }

  private registerLoginSubscriptions() {
    this.loginStateSubscription = this.successfulLogin$.subscribe(
      this.handleSucessfulLogin.bind(this)
    );
  }

  private handleSucessfulLogin() {
    if (this.dialogRef) {
      this.dialogRef.close();
    }
  }

  ngOnDestroy(): void {
    if (this.loginStateSubscription) {
      this.loginStateSubscription.unsubscribe();
    }
  }
}
