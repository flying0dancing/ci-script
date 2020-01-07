import { UsersActions } from "@/core/api/users";
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Store } from "@ngrx/store";
import { NAMESPACE } from "../../users/users.constants";

@Component({
  selector: "app-create-user-form",
  templateUrl: "./create-user-form.component.html",
  styleUrls: ["./create-user-form.component.scss"]
})
export class CreateUserFormComponent {
  public createUserForm: FormGroup = this.fb.group({
    username: ["", Validators.required],
    password: ["", [Validators.required, , Validators.minLength(6)]]
  });

  constructor(private fb: FormBuilder, private store: Store<any>) {}

  public onSubmit() {
    const { username, password } = this.createUserForm.getRawValue();
    this.store.dispatch(
      new UsersActions.Post({ reducerMapKey: NAMESPACE, username, password })
    );
  }

  get username() {
    return this.createUserForm.get("username");
  }

  get password() {
    return this.createUserForm.get("password");
  }
}
