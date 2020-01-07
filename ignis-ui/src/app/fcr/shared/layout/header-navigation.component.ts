import * as AuthActions from '@/core/api/auth/auth.actions';
import { NAMESPACE as AUTH_NAMESPACE } from '@/core/api/auth/auth.constants';
import { AuthService } from '@/core/api/auth/auth.service';
import * as FeaturesActions from '@/core/api/features/features.actions';
import { NAMESPACE as FEATURES_NAMESPACE } from '@/core/api/features/features.reducer';
import * as UsersActions from '@/core/api/users/users.actions';
import { NAMESPACE as USERS_NAMESPACE } from '@/core/api/users/users.constants';
import { User } from '@/core/api/users/users.interfaces';
import * as UsersSelectors from '@/core/api/users/users.selectors';
import { ChangePasswordDialogComponent } from '@/fcr/dashboard/change-password/change-password-dialog.component';
import { UsersDialogComponent } from '@/fcr/dashboard/users/users-dialog.component';
import { CalendarDialogComponent } from '@/fcr/shared/calendar/calendar-dialog.component';
import { DialogsConstants } from '@/shared/dialogs';
import { Component, OnDestroy } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { host } from '@env/environment.prod';
import { Store } from '@ngrx/store';
import { Observable, Subscription } from 'rxjs';
import { filter, map, pairwise, take, tap } from 'rxjs/operators';

@Component({
  selector: 'app-header-navigation',
  templateUrl: './header-navigation.component.html',
  styleUrls: ['./header-navigation.component.scss']
})
export class HeaderNavigationComponent implements OnDestroy {
  username$: Observable<string> = this.store
    .select(UsersSelectors.getCurrentUser)
    .pipe(
      tap((user: User) => {
        if (!user) {
          this.store.dispatch(
            new UsersActions.GetCurrentUser({ reducerMapKey: USERS_NAMESPACE })
          );
        }
      }),
      filter(user => !!user),
      map((user: User) => user.username)
    );

  restDocsUrl: string;

  private usersDialogRef: MatDialogRef<UsersDialogComponent>;
  private changePasswordDialogRef: MatDialogRef<ChangePasswordDialogComponent>;
  private calendarDialogRef: MatDialogRef<CalendarDialogComponent>;
  private changePasswordState$: Observable<any> = this.store.select(
    UsersSelectors.getUsersChangePasswordState
  );
  private successfulChangePasswordPost$ = this.changePasswordState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev.loading && !curr.loading && !curr.error)
  );
  private successfulPasswordChangeSubscription: Subscription;
  private featureActiveSubscription: Subscription;

  constructor(
    private store: Store<any>,
    public router: Router,
    public service: AuthService,
    private dialog: MatDialog,
    private snackbar: MatSnackBar
  ) {
    this.successfulPasswordChangeSubscription = this.successfulChangePasswordPost$.subscribe(
      this.handleChangePasswordSuccessfulPost.bind(this)
    );

    this.store.dispatch(
      new FeaturesActions.Get({ reducerMapKey: FEATURES_NAMESPACE })
    );
    this.restDocsUrl = `${host}/swagger-ui.html`;
  }

  private handleChangePasswordSuccessfulPost() {
    this.snackbar.open(`Password change successful`, undefined, {
      duration: 3000
    });
    this.handleCloseChangePasswordDialog();
  }

  private handleCloseChangePasswordDialog() {
    if (this.changePasswordDialogRef) {
      this.changePasswordDialogRef.close();
    }
  }

  handleLogoutClick() {
    this.store.dispatch(
      new AuthActions.Logout({ reducerMapKey: AUTH_NAMESPACE })
    );
  }

  handleUsersClick() {
    this.usersDialogRef = this.dialog.open(UsersDialogComponent, {
      width: DialogsConstants.WIDTHS.SMALL
    });

    this.usersDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (this.usersDialogRef = undefined));
  }

  handleChangePasswordClick() {
    this.changePasswordDialogRef = this.dialog.open(
      ChangePasswordDialogComponent,
      {
        width: DialogsConstants.WIDTHS.SMALL
      }
    );

    this.changePasswordDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (this.changePasswordDialogRef = undefined));
  }

  handleCalendarClick() {
    this.calendarDialogRef = this.dialog.open(CalendarDialogComponent, {
      width: DialogsConstants.WIDTHS.MEDIUM
    });
  }

  ngOnDestroy() {
    if (this.successfulPasswordChangeSubscription) {
      this.successfulPasswordChangeSubscription.unsubscribe();
    }
    if (this.featureActiveSubscription) {
      this.featureActiveSubscription.unsubscribe();
    }
  }
}
