import { quietHeaders } from "@/core/interceptors/http-error/http-error.interceptor";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";
import * as UsersInterfaces from "./users.interfaces";

@Injectable()
export class UsersService {
  constructor(private http: HttpClient) {}

  static generateAuthUrl(): string {
    return `${environment.api.internalRoot}/users`;
  }

  static generateUsersUrl(user = "currentUser"): string {
    return `${UsersService.generateAuthUrl()}/${user}`;
  }

  changePassword(oldPassword: string, newPassword: string) {
    return this.http.put(
      UsersService.generateUsersUrl(),
      { oldPassword, newPassword },
      { withCredentials: true, headers: quietHeaders() }
    );
  }

  getUserByName(
    username: string = "currentUser"
  ): Observable<UsersInterfaces.User> {
    return this.http.get<UsersInterfaces.User>(
      UsersService.generateUsersUrl(username),
      { withCredentials: true }
    );
  }

  getUsers(): Observable<UsersInterfaces.GetUsersResponse> {
    return this.http.get<UsersInterfaces.GetUsersResponse>(
      UsersService.generateAuthUrl(),
      { withCredentials: true }
    );
  }
}
