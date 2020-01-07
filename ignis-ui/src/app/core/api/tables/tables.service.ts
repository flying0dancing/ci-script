import { HttpClient, HttpEvent, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

import { GetTablesResponse, UploadResponse } from "./tables.interfaces";
import * as TableTypes from "./tables.types";

@Injectable()
export class TablesService {
  constructor(private http: HttpClient) {}

  static generateUrl(): string {
    return `${environment.api.internalRoot}/tables`;
  }

  static generateTableIdUrl(id: TableTypes.Id): string {
    return `${TablesService.generateUrl()}/${id}`;
  }

  static generateFileUploadUrl(): string {
    return `${TablesService.generateUrl()}/file`;
  }

  getTables(): Observable<GetTablesResponse> {
    return this.http.get<GetTablesResponse>(
      `${TablesService.generateUrl()}?sort=displayName,asc`
    );
  }

  upload({
    formData
  }: {
    formData: FormData;
  }): Observable<HttpEvent<UploadResponse>> {
    const request = new HttpRequest(
      "POST",
      TablesService.generateFileUploadUrl(),
      formData,
      {
        reportProgress: true,
        withCredentials: true
      }
    );

    return this.http.request<UploadResponse>(request);
  }

  delete(id: TableTypes.Id): Observable<Response> {
    return (<any>this.http).delete(TablesService.generateTableIdUrl(id));
  }
}
