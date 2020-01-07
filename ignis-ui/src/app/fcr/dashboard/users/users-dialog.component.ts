import * as UsersActions from "@/core/api/users/users.actions";
import { DialogsConstants } from "@/shared/dialogs";
import { Component, OnDestroy } from "@angular/core";
import {
  MatDialog,
  MatDialogConfig,
  MatDialogRef
} from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Store } from "@ngrx/store";
import { Observable, Subscription } from "rxjs";
import { filter, pairwise, take } from "rxjs/operators";
import { NAMESPACE } from "../users/users.constants";
import { CreateUserDialogComponent } from "./create-user/create-user-dialog.component";
import * as UsersSelectors from "./users.selectors";

@Component({
  selector: "app-users-dialog",
  templateUrl: "./users-dialog.component.html",
  styleUrls: ["./users-dialog.component.scss"]
})
export class UsersDialogComponent implements OnDestroy {
  private createUserDialogRef: MatDialogRef<CreateUserDialogComponent>;
  public usersCollection$: Observable<any> = this.store.select(
    UsersSelectors.getUsersCollection
  );
  public usersGetLoading$: Observable<any> = this.store.select(
    UsersSelectors.getUsersLoading
  );
  private createUserPostState$: Observable<any> = this.store.select(
    UsersSelectors.getUsersPostState
  );
  private successfulCreateUserPost$ = this.createUserPostState$.pipe(
    pairwise(),
    filter(([prev, curr]) => prev.loading && !curr.loading && !curr.error)
  );
  private successfulUserCreationSubscription: Subscription;
  private createUserDialogConfig: MatDialogConfig = {
    width: DialogsConstants.WIDTHS.SMALL
  };

  constructor(
    private store: Store<any>,
    private dialog: MatDialog,
    private snackbar: MatSnackBar
  ) {
    this.getUsers();
    this.registerCreateUserSubscriptions();
  }

  private getUsers() {
    this.store.dispatch(new UsersActions.Get({ reducerMapKey: NAMESPACE }));
  }

  private openCreateUserDialog() {
    this.createUserDialogRef = this.dialog.open(
      CreateUserDialogComponent,
      this.createUserDialogConfig
    );
  }

  public handleAddUserButtonClick() {
    this.openCreateUserDialog();

    this.createUserDialogRef
      .afterClosed()
      .pipe(take(1))
      .subscribe(() => (this.createUserDialogRef = undefined));
  }

  private handleCloseCreateUserDialog() {
    if (this.createUserDialogRef) {
      this.createUserDialogRef.close();
    }
  }

  private registerCreateUserSubscriptions() {
    this.successfulUserCreationSubscription = this.successfulCreateUserPost$.subscribe(
      this.handleSuccessfulCreateUserPost.bind(this)
    );
  }

  private handleSuccessfulCreateUserPost() {
    this.getUsers();
    this.handleCloseCreateUserDialog();
    this.snackbar.open("New user created", undefined, { duration: 3000 });
  }

  ngOnDestroy() {
    this.successfulUserCreationSubscription.unsubscribe();
  }
}
