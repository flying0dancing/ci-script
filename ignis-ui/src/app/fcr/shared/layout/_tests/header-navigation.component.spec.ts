import { AuthService } from "@/core/api/auth";
import { Logout } from "@/core/api/auth/auth.actions";
import { NAMESPACE as AUTH_NAMESPACE } from "@/core/api/auth/auth.constants";
import { Get } from "@/core/api/features/features.actions";
import { NAMESPACE } from "@/core/api/features/features.reducer";

import { UsersActions, UsersConstants } from "@/core/api/users";
import { newPassword, oldPassword } from "@/core/api/users/_tests/users.mocks";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { ChangePasswordDialogComponent } from "@/fcr/dashboard/change-password/change-password-dialog.component";
import { ChangePasswordFormComponent } from "@/fcr/dashboard/change-password/change-password-form.component";
import { UsersDialogComponent } from "@/fcr/dashboard/users/users-dialog.component";
import { DialogHelpers } from "@/test-helpers";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import {
  async,
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { MatButtonModule } from "@angular/material/button";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatDividerModule } from "@angular/material/divider";
import { MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from "@angular/material/menu";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { MatToolbarModule } from "@angular/material/toolbar";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { Store, StoreModule } from "@ngrx/store";
import { of } from "rxjs";
import { HeaderNavigationComponent } from "../header-navigation.component";

describe("HeaderNavigationComponent", () => {
  let component: HeaderNavigationComponent;
  let fixture: ComponentFixture<HeaderNavigationComponent>;
  let store: Store<any>;
  let dialog: MatDialog;
  let dialogSpy: any;
  let dispatchSpy: jasmine.Spy;
  let snackBar: MatSnackBar;
  let subscribeSpy: any;
  let router: Router;
  let service: AuthService;
  const reducerMapKey = UsersConstants.NAMESPACE;
  const mockChangePasswordPost = { reducerMapKey, oldPassword, newPassword };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        MatButtonModule,
        MatDialogModule,
        MatIconModule,
        MatMenuModule,
        MatToolbarModule,
        MatSnackBarModule,
        MatDividerModule,
        StoreModule.forRoot(reducers),
        RouterTestingModule,
        HttpClientTestingModule,
        ReactiveFormsModule,
        DialogHelpers.createDialogTestingModule({
          declarations: [
            ChangePasswordDialogComponent,
            ChangePasswordFormComponent,
            UsersDialogComponent
          ],
          entryComponents: [
            ChangePasswordDialogComponent,
            ChangePasswordFormComponent,
            UsersDialogComponent
          ],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      declarations: [HeaderNavigationComponent],
      providers: [
        AuthService,
        {
          provide: MatSnackBar,
          useValue: { open: jasmine.createSpy("openSnackBar") }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    dialog = TestBed.get(MatDialog);
    snackBar = TestBed.get(MatSnackBar);
    store = TestBed.get(Store);
    router = TestBed.get(Router);
    service = TestBed.get(AuthService);
    fixture = TestBed.createComponent(HeaderNavigationComponent);
    component = fixture.componentInstance;
    dialogSpy = jasmine.createSpy("dialogSpy");
    subscribeSpy = jasmine.createSpy("subscribeSpy");
    dispatchSpy = spyOn(store, "dispatch");
    dispatchSpy.and.callThrough();

    fixture.detectChanges();
  });

  describe("init", () => {
    it("should get all feature flags", () => {
      fixture = TestBed.createComponent(HeaderNavigationComponent);
      component = fixture.componentInstance;

      expect(store.dispatch).toHaveBeenCalledWith(
        new Get({ reducerMapKey: NAMESPACE })
      );
    });
  });

  describe("successfulChangePasswordPost$", () => {
    it("should not emit if there has been no successful change password post", () => {
      (<any>component).successfulChangePasswordPost$.subscribe(subscribeSpy);

      store.dispatch(new UsersActions.ChangePassword(mockChangePasswordPost));
      store.dispatch(
        new UsersActions.ChangePasswordFail({ reducerMapKey, statusCode: 401 })
      );

      expect(subscribeSpy).toHaveBeenCalledTimes(0);
    });

    it("should only emit if there has been a successful change password post", () => {
      (<any>component).successfulChangePasswordPost$.subscribe(subscribeSpy);

      store.dispatch(new UsersActions.ChangePassword(mockChangePasswordPost));
      store.dispatch(new UsersActions.ChangePasswordSuccess({ reducerMapKey }));

      expect(subscribeSpy).toHaveBeenCalledTimes(1);
    });
  });

  describe("handleLogoutClick", () => {
    beforeEach(() => {
      spyOn(service, "logout").and.returnValue(of({}));
      spyOn(router, "navigate");
      (<any>component).handleLogoutClick();
    });

    it("should dispatch logout event", () => {
      expect(store.dispatch).toHaveBeenLastCalledWith(
        new Logout({ reducerMapKey: AUTH_NAMESPACE })
      );
    });
  });

  describe("handleUsersClick", () => {
    beforeEach(() => {
      (<any>component).handleUsersClick();
    });

    it("should open the users dialog", fakeAsync(() => {
      fixture.detectChanges();

      tick(500);

      expect((<any>component).usersDialogRef).toBeDefined();
    }));

    it("should remove the users dialog reference on close", fakeAsync(() => {
      dialog.closeAll();

      fixture.detectChanges();

      tick(500);

      expect((<any>component).usersDialogRef).toBeUndefined();
    }));
  });

  describe("handleChangePasswordClick", () => {
    beforeEach(() => {
      (<any>component).handleChangePasswordClick();
    });

    it("should open the change password dialog", fakeAsync(() => {
      fixture.detectChanges();

      tick(500);

      expect((<any>component).changePasswordDialogRef).toBeDefined();
    }));

    it("should remove the change password dialog reference on close", fakeAsync(() => {
      dialog.closeAll();

      fixture.detectChanges();

      tick(500);

      expect((<any>component).changePasswordDialogRef).toBeUndefined();
    }));
  });

  describe("handleChangePasswordSuccessfulPost", () => {
    beforeEach(() => {
      spyOn(
        <any>component,
        "handleCloseChangePasswordDialog"
      ).and.callThrough();
      (<any>component).handleChangePasswordClick();
      (<any>component).handleChangePasswordSuccessfulPost();
    });

    it("should close the create user dialog if it is already open", fakeAsync(() => {
      expect(
        (<any>component).handleCloseChangePasswordDialog
      ).toHaveBeenCalledTimes(1);
    }));

    it("should show a snackbar", fakeAsync(() => {
      expect(snackBar.open).toHaveBeenCalledWith(
        "Password change successful",
        undefined,
        { duration: 3000 }
      );
    }));
  });

  describe("handleCloseChangePasswordDialog", () => {
    beforeEach(() => {
      (<any>component).handleChangePasswordClick();
      spyOn(
        (<any>component).changePasswordDialogRef,
        "close"
      ).and.callThrough();
    });

    it("should close the CreateUserDialog if it is open", fakeAsync(() => {
      (<any>component).handleCloseChangePasswordDialog();
      expect(
        (<any>component).changePasswordDialogRef.close
      ).toHaveBeenCalledTimes(1);
    }));

    it("should not close the create user dialog if it is no longer open", fakeAsync(() => {
      (<any>component).changePasswordDialogRef = undefined;
      (<any>component).handleCloseChangePasswordDialog(dialogSpy);
      expect(dialogSpy).toHaveBeenCalledTimes(0);
    }));
  });

  it("should have username if user in store", () => {
    store.dispatch(
      new UsersActions.GetCurrentUserSuccess({
        reducerMapKey: UsersConstants.NAMESPACE,
        currentUser: { id: 1, username: "me" }
      })
    );

    let username: string;
    component.username$.subscribe(result => (username = result));

    expect(username).toEqual("me");
  });

  it("should have username if store empty", () => {
    store.dispatch(
      new UsersActions.Empty({ reducerMapKey: UsersConstants.NAMESPACE })
    );

    let username: string;
    component.username$.subscribe(result => (username = result));

    expect(username).toBeUndefined();
  });

  it("should dispatch if store empty", () => {
    store.dispatch(
      new UsersActions.Empty({ reducerMapKey: UsersConstants.NAMESPACE })
    );

    component.username$.subscribe(console.log);

    expect(dispatchSpy).toHaveBeenLastCalledWith(
      new UsersActions.GetCurrentUser({
        reducerMapKey: UsersConstants.NAMESPACE
      })
    );
  });
});
