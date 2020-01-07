import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { host } from '@env/environment';

@Injectable()
export class AuthService {
  constructor(private http: HttpClient) {}

  static generateAuthUrl(): string {
    return `${host}/auth`;
  }

  static generateUsersUrl(): string {
    return `${AuthService.generateAuthUrl()}/users`;
  }

  login(username: string, password: string) {
    let headers = new HttpHeaders();
    headers = headers.append(
      'Authorization',
      'Basic ' + btoa(`${username}:${password}`)
    );

    return this.http.post(
      `${AuthService.generateAuthUrl()}/login`,
      {},
      { headers }
    );
  }

  logout() {
    return this.http.post(
      `${AuthService.generateAuthUrl()}/logout`,
      {},
      { withCredentials: true }
    );
  }

  createUser(username: string, password: string) {
    return this.http.post(
      AuthService.generateUsersUrl(),
      { username, password },
      { withCredentials: true }
    );
  }
}
