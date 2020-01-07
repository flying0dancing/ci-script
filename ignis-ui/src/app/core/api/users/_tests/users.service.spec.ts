import { Type } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { UsersService } from "../users.service";
import {
  getUsersResponse,
  newPassword,
  oldPassword,
  user,
  username
} from "./users.mocks";

describe("UsersService", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: UsersService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UsersService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(UsersService);
  });

  it("should get all users of the system", () => {
    const expected = getUsersResponse;

    let actual;

    service.getUsers().subscribe(response => (actual = response));

    const req = httpMock.expectOne(`${UsersService.generateAuthUrl()}`);

    req.flush(getUsersResponse);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should get a user details by a specified name", () => {
    const expected = user;

    let actual;

    service.getUserByName(username).subscribe(response => (actual = response));

    const req = httpMock.expectOne(UsersService.generateUsersUrl(username));

    req.flush(user);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should get a user details by currentUser if not specified", () => {
    const expected = user;

    let actual;

    service.getUserByName().subscribe(response => (actual = response));

    const req = httpMock.expectOne(UsersService.generateUsersUrl());

    req.flush(user);

    expect(actual).toEqual(expected);
  });

  it("should post new password details", () => {
    let actual;

    service
      .changePassword(oldPassword, newPassword)
      .subscribe(response => (actual = response));

    const req = httpMock.expectOne(`${UsersService.generateUsersUrl()}`);

    req.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });
});
