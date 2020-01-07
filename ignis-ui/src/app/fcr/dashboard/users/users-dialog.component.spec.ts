import { UsersActions, UsersConstants } from "@/core/api/users";
import { newPassword, username } from "@/core/api/users/_tests/users.mocks";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { DialogHelpers } from "@/test-helpers";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick
} from "@angular/core/testing";
import { MatDialog, MatDialogModule } from "@angular/material/dialog";
import { MatSnackBar, MatSnackBarModule } from "@angular/material/snack-bar";
import { Store, StoreModule } from "@ngrx/store";
import { CreateUserDialogComponent } from "./create-user/create-user-dialog.component";
import { UsersDialogComponent } from "./users-dialog.component";

describe("UsersDialogComponent", () => {
  let component: UsersDialogComponent;
  let fixture: ComponentFixture<UsersDialogComponent>;
  let store: Store<any>;
  let dialog: MatDialog;
  let dialogSpy: any;
  let snackBar: MatSnackBar;
  let subscribeSpy: any;
  const reducerMapKey = UsersConstants.NAMESPACE;
  const mockCreateUserPost = { reducerMapKey, username, password: newPassword };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MatDialogModule,
        MatSnackBarModule,
        StoreModule.forRoot(reducers),
        DialogHelpers.createDialogTestingModule({
          declarations: [CreateUserDialogComponent],
          entryComponents: [CreateUserDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      declarations: [UsersDialogComponent],
      providers: [
        {
          provide: MatSnackBar,
          useValue: { open: jasmine.createSpy("openSnackBar") }
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    dialog = TestBed.get(MatDialog);
    snackBar = TestBed.get(MatSnackBar);
    store = TestBed.get(Store);
    fixture = TestBed.createComponent(UsersDialogComponent);
    component = fixture.componentInstance;
    dialogSpy = jasmine.createSpy("dialogSpy");
    subscribeSpy = jasmine.createSpy("subscribeSpy");

    spyOn(store, "dispatch").and.callThrough();

    fixture.detectChanges();
  });

  describe("openCreateUserDialog", () => {
    beforeEach(() => {
      spyOn(dialog, "open").and.callThrough();
    });

    it("should open the editor dialog", () => {
      (<any>component).openCreateUserDialog();

      expect(dialog.open).toHaveBeenCalledWith(
        CreateUserDialogComponent,
        (<any>component).createUserDialogConfig
      );
    });
  });

  describe("handleAddUserButtonClick", () => {
    beforeEach(() => {
      (<any>component).handleAddUserButtonClick();
    });

    it("should open the create user dialog", fakeAsync(() => {
      fixture.detectChanges();

      tick(500);

      expect((<any>component).createUserDialogRef).toBeDefined();
    }));

    it("should remove the create user dialog reference on close", fakeAsync(() => {
      dialog.closeAll();

      fixture.detectChanges();

      tick(500);

      expect((<any>component).createUserDialogRef).toBeUndefined();
    }));
  });

  describe("handleSuccessfulCreateUserPost", () => {
    beforeEach(() => {
      spyOn(<any>component, "getUsers").and.callThrough();
      spyOn(<any>component, "handleCloseCreateUserDialog").and.callThrough();
      (<any>component).openCreateUserDialog();
      (<any>component).handleSuccessfulCreateUserPost();
    });

    it("should get all users", () => {
      expect((<any>component).getUsers).toHaveBeenCalledTimes(1);
    });

    it("should close the create user dialog if it is already open", fakeAsync(() => {
      expect(
        (<any>component).handleCloseCreateUserDialog
      ).toHaveBeenCalledTimes(1);
    }));

    it("should show a snackbar", fakeAsync(() => {
      expect(snackBar.open).toHaveBeenCalledWith(
        "New user created",
        undefined,
        { duration: 3000 }
      );
    }));
  });

  describe("handleCloseCreateUserDialog", () => {
    beforeEach(() => {
      (<any>component).openCreateUserDialog();
      spyOn((<any>component).createUserDialogRef, "close").and.callThrough();
    });

    it("should close the CreateUserDialog if it is open", fakeAsync(() => {
      (<any>component).handleCloseCreateUserDialog();
      expect((<any>component).createUserDialogRef.close).toHaveBeenCalledTimes(
        1
      );
    }));

    it("should not close the create user dialog if it is no longer open", fakeAsync(() => {
      (<any>component).createUserDialogRef = undefined;
      (<any>component).handleCloseCreateUserDialog(dialogSpy);
      expect(dialogSpy).toHaveBeenCalledTimes(0);
    }));
  });

  describe("successfulCreateUserPost$", () => {
    it("should not emit if there has been no successful create user post", () => {
      (<any>component).successfulCreateUserPost$.subscribe(subscribeSpy);

      store.dispatch(new UsersActions.Post(mockCreateUserPost));
      store.dispatch(new UsersActions.PostFail({ reducerMapKey }));

      expect(subscribeSpy).toHaveBeenCalledTimes(0);
    });

    it("should only emit if there has been a successful create user post", () => {
      (<any>component).successfulCreateUserPost$.subscribe(subscribeSpy);

      store.dispatch(new UsersActions.Post(mockCreateUserPost));
      store.dispatch(new UsersActions.PostSuccess({ reducerMapKey }));

      expect(subscribeSpy).toHaveBeenCalledTimes(1);
    });
  });
});
