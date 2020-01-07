import { Type } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { AuthService } from "../auth.service";
import { password, username } from "./auth.mocks";

describe("AuthService", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(AuthService);
  });

  it("should post login details", () => {
    let actual;

    service
      .login(username, password)
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(`${AuthService.generateAuthUrl()}/login`);

    req.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });

  it("should post to logout", () => {
    let actual;

    service.logout().subscribe(response => (actual = response));

    const req = httpMock.expectOne(`${AuthService.generateAuthUrl()}/logout`);

    req.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });

  it("should post new user details details", () => {
    let actual;

    service
      .createUser(username, password)
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(`${AuthService.generateUsersUrl()}`);

    req.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });
});
