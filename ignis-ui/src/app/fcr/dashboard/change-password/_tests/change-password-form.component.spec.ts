import { UsersActions, UsersConstants } from "@/core/api/users";
import { newPassword, oldPassword } from "@/core/api/users/_tests/users.mocks";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { Store, StoreModule } from "@ngrx/store";
import { reducers } from "../../_tests/dashboard-reducers.mock";
import { ChangePasswordFormComponent } from "../change-password-form.component";

describe("ChangePasswordFormComponent", () => {
  let comp: ChangePasswordFormComponent;
  let fixture: ComponentFixture<ChangePasswordFormComponent>;
  let store: Store<any>;
  let form: FormGroup;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        StoreModule.forRoot(reducers)
      ],
      declarations: [ChangePasswordFormComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });

    form = new FormGroup({
      oldPassword: new FormControl(null),
      newPassword: new FormControl(null)
    });

    fixture = TestBed.createComponent(ChangePasswordFormComponent);
    comp = fixture.componentInstance;
    comp.changePasswordForm = form;
    store = TestBed.get(Store);

    spyOn(store, "dispatch").and.callThrough();

    fixture.detectChanges();
  });

  describe("onSubmit", () => {
    it("should dispatch the change password action with new and old passwords", () => {
      const action = new UsersActions.ChangePassword({
        reducerMapKey: UsersConstants.NAMESPACE,
        oldPassword: oldPassword,
        newPassword: newPassword
      });
      const oldPasswordControl = comp.changePasswordForm.get("oldPassword");
      const newPasswordControl = comp.changePasswordForm.get("newPassword");
      oldPasswordControl.setValue(oldPassword, { emitEvent: false });
      newPasswordControl.setValue(newPassword, { emitEvent: false });

      comp.onSubmit();
      expect(store.dispatch).toHaveBeenCalledWith(action);
    });
  });
});
