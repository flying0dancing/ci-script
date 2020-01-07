import * as UsersActions from "@/core/api/users/users.actions";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { Store, StoreModule } from "@ngrx/store";
import { reducers } from "../../../_tests/dashboard-reducers.mock";
import { NAMESPACE } from "../../../users/users.constants";
import { CreateUserFormComponent } from "../create-user-form.component";

describe("CreateUserFormComponent", () => {
  let comp: CreateUserFormComponent;
  let fixture: ComponentFixture<CreateUserFormComponent>;
  let store: Store<any>;
  let form: FormGroup;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        StoreModule.forRoot(reducers)
      ],
      declarations: [CreateUserFormComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });

    form = new FormGroup({
      oldPassword: new FormControl(null),
      newPassword: new FormControl(null)
    });

    fixture = TestBed.createComponent(CreateUserFormComponent);
    comp = fixture.componentInstance;
    comp.createUserForm = form;

    store = TestBed.get(Store);

    spyOn(store, "dispatch").and.callThrough();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUserFormComponent);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe("onSubmit", () => {
    it("should dispatch the post user action to create a new user with username and password", () => {
      const action = new UsersActions.Post({
        reducerMapKey: NAMESPACE,
        username: "username",
        password: "password"
      });
      const usernameControl = comp.createUserForm.get("username");
      const passwordControl = comp.createUserForm.get("password");
      usernameControl.setValue("username", { emitEvent: false });
      passwordControl.setValue("password", { emitEvent: false });

      comp.onSubmit();
      expect(store.dispatch).toHaveBeenCalledWith(action);
    });
  });
});
