import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { RuleRequest, Schema } from '..';
import { EnvironmentService } from '../../../environments/environment.service';
import { quietHeaders } from '../../core/interceptors/http-error/http-error.interceptor';
import { Identifiable } from '../../core/utilities/interfaces/indentifiable.interface';
import { UpdateSchemaRequest } from '../../schema-details/interfaces/update-schema-request.interface';
import { CopySchemaRequest } from '../interfaces/copy-schema-request.interface';
import { CreateSchemaResponse } from '../interfaces/create-response.interface';
import { CreateSchemaRequest } from '../interfaces/create-schema-request.interface';

@Injectable({ providedIn: 'root' })
export class SchemasRepository {
  readonly productsUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.productsUrl = `${this.environmentService.env.api.root}/productConfigs`;
  }

  private tablesUrl(productId: number) {
    return `${this.productsUrl}/${productId}/schemas`;
  }

  create(productId: number, schemaRequest: CreateSchemaRequest): Observable<CreateSchemaResponse> {
    return this.http.post<CreateSchemaResponse>(`${this.productsUrl}/${productId}/schemas`, schemaRequest);
  }

  createNewVersions(
    productId: number,
    schemaId: number,
    startDate: string
  ): Observable<CreateSchemaResponse> {
    return this.http.post<CreateSchemaResponse>(
      `${this.productsUrl}/${productId}/schemas/${schemaId}`,
      {
        startDate: startDate
      }
    );
  }

  copy(productId: number, schemaId: number, copyRequest: CopySchemaRequest): Observable<CreateSchemaResponse> {
    return this.http.post<CreateSchemaResponse>(
      `${this.productsUrl}/${productId}/schemas/${schemaId}/copy`, { ...copyRequest });
  }

  getById(productId: number, id: number): Observable<Schema> {
    return this.http.get<Schema>(`${this.tablesUrl(productId)}/${id}`);
  }

  createExampleCsv(productId: number, id: number): Observable<void> {
    return this.http.get<void>(`${this.tablesUrl(productId)}/${id}/exampleCsv`);
  }
  createExampleCsvUrl(productId: number, id: number): string {
    return `${this.tablesUrl(productId)}/${id}/exampleCsv`;
  }

  delete(productId: number, id: number): Observable<number> {
    return this.http
      .delete<Identifiable>(`${this.tablesUrl(productId)}/${id}`)
      .pipe(map(response => response.id));
  }

  updateSchemaDetails(
    productId: number,
    schemaId: number,
    request: UpdateSchemaRequest
  ): Observable<number> {
    return this.http.patch<number>(
      `${this.tablesUrl(productId)}/${schemaId}`,
      request
    );
  }

  addRule(productId: number, schemaId: number, ruleRequest: RuleRequest) {
    const headers = quietHeaders();
    return this.http.post(
      `${this.tablesUrl(productId)}/${schemaId}/rules`,
      ruleRequest,
      { headers }
    );
  }
}
