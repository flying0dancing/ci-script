import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

import { GetDatasetsResponse } from "./datasets.interfaces";
import * as DatasetsTypes from "./datasets.types";

@Injectable()
export class DatasetsService {
  constructor(private http: HttpClient) {}

  static generateGetUrl(): string {
    return `${environment.api.externalRoot}/datasets`;
  }

  static generateGetSourceFilesUrl(): string {
    return `${environment.api.internalRoot}/datasets/source/files`;
  }

  get(): Observable<GetDatasetsResponse> {
    return this.http.get<GetDatasetsResponse>(
      `${DatasetsService.generateGetUrl()}?sort=name,asc`
    );
  }

  getSourceFiles(): Observable<DatasetsTypes.SourceFiles> {
    return this.http.get<DatasetsTypes.SourceFiles>(
      DatasetsService.generateGetSourceFilesUrl()
    );
  }
}
