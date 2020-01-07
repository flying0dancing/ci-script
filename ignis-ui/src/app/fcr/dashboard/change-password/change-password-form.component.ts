import { UsersActions, UsersSelectors } from "@/core/api/users/";
import { NAMESPACE } from "@/core/api/users/users.constants";
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";

@Component({
  selector: "app-change-password-form",
  templateUrl: "./change-password-form.component.html",
  styleUrls: ["./change-password-form.component.scss"]
})
export class ChangePasswordFormComponent {
  public usersChangePasswordLoadingState$: Observable<any> = this.store.select(
    UsersSelectors.getUsersChangePasswordLoadingState
  );
  public usersChangePasswordErrorState$: Observable<any> = this.store.select(
    UsersSelectors.getUsersChangePasswordErrorState
  );
  public changePasswordForm: FormGroup = this.fb.group({
    oldPassword: ["", [Validators.required, Validators.minLength(6)]],
    newPassword: ["", [Validators.required, Validators.minLength(6)]]
  });

  constructor(private fb: FormBuilder, private store: Store<any>) {}

  public onSubmit() {
    const { oldPassword, newPassword } = this.changePasswordForm.getRawValue();
    this.store.dispatch(
      new UsersActions.ChangePassword({
        reducerMapKey: NAMESPACE,
        oldPassword,
        newPassword
      })
    );
  }

  public clearErrors(): void {
    this.store.dispatch(
      new UsersActions.ClearPasswordErrors({ reducerMapKey: NAMESPACE })
    );
  }

  get oldPassword() {
    return this.changePasswordForm.get("oldPassword");
  }

  get newPassword() {
    return this.changePasswordForm.get("newPassword");
  }
}
