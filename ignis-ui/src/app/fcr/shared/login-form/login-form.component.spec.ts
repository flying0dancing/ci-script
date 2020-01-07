import { AuthActions, AuthConstants } from "@/core/api/auth";
import { NAMESPACE } from "@/core/api/auth/auth.constants";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { ShellComponent } from "@/test-helpers/component.helpers";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { Store, StoreModule } from "@ngrx/store";
import { LoginFormComponent } from "./login-form.component";

describe("LoginFormComponent", () => {
  let component: LoginFormComponent;
  let fixture: ComponentFixture<LoginFormComponent>;
  let store: Store<any>;
  let form: FormGroup;
  let router: Router;
  let subscribeSpy: any;
  const reducerMapKey = AuthConstants.NAMESPACE;
  const mockLoginBody = {
    reducerMapKey,
    username: "username",
    password: "password",
    refererUrl: null
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        RouterTestingModule.withRoutes([
          { path: "redirect", component: ShellComponent }
        ]),
        StoreModule.forRoot(reducers)
      ],
      declarations: [LoginFormComponent, ShellComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });

    form = new FormGroup({
      username: new FormControl(null),
      password: new FormControl(null)
    });

    fixture = TestBed.createComponent(LoginFormComponent);
    component = fixture.componentInstance;
    component.loginForm = form;
    component.redirect = "/redirect";

    store = TestBed.get(Store);
    router = TestBed.get(Router);

    spyOn(store, "dispatch").and.callThrough();
    subscribeSpy = jasmine.createSpy("subscribeSpy");
    fixture.detectChanges();
  });

  describe("successfulLogin$", () => {
    it("should not emit if there has been no successful login", () => {
      (<any>component).successfulLogin$.subscribe(subscribeSpy);

      store.dispatch(new AuthActions.Login(mockLoginBody));
      store.dispatch(
        new AuthActions.LoginFail({
          reducerMapKey,
          error: { error: { message: "error message" } }
        })
      );

      expect(subscribeSpy).toHaveBeenCalledTimes(0);
    });

    it("should only emit if there has been a successful login", () => {
      (<any>component).successfulLogin$.subscribe(subscribeSpy);

      store.dispatch(new AuthActions.Login(mockLoginBody));
      store.dispatch(new AuthActions.LoginSuccess({ reducerMapKey }));

      expect(subscribeSpy).toHaveBeenCalledTimes(1);
    });
  });

  describe("onSubmit", () => {
    it("should dispatch the login auth action with username and password", () => {
      const action = new AuthActions.Login({
        reducerMapKey: NAMESPACE,
        username: "username",
        password: "password",
        refererUrl: null
      });
      const usernameControl = component.loginForm.get("username");
      const passwordControl = component.loginForm.get("password");
      usernameControl.setValue("username", { emitEvent: false });
      passwordControl.setValue("password", { emitEvent: false });

      component.onSubmit();
      expect(store.dispatch).toHaveBeenCalledWith(action);
    });
  });

  describe("handleSucessfulLogin", () => {
    it("should navigate the router to the value of redirect", () => {
      spyOn(router, "navigate");
      (<any>component).handleSucessfulLogin();
      expect(router.navigate).toHaveBeenCalledTimes(1);
      expect(router.navigate).toHaveBeenCalledWith(["/redirect"]);
    });
  });

  describe("ngOnInit", () => {
    beforeEach(() => {
      spyOn(<any>component, "registerLoginSubscriptions");
    });
    it("should call registerLoginSubscriptions when a value for redirect is present", () => {
      component.ngOnInit();
      expect((<any>component).registerLoginSubscriptions).toHaveBeenCalledTimes(
        1
      );
    });

    it("should not call registerLoginSubscriptions when no redirect value is present", () => {
      component.redirect = null;
      component.ngOnInit();
      expect((<any>component).registerLoginSubscriptions).toHaveBeenCalledTimes(
        0
      );
    });
  });
});
