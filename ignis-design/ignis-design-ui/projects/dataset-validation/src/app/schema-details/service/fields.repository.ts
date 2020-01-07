import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentService } from '../../../environments/environment.service';
import { Field } from '../../schemas';

@Injectable({ providedIn: 'root' })
export class FieldsRepository {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.baseUrl = `${this.environmentService.env.api.root}`;
  }

  fieldsUrl(productId: number, schemaId: number) {
    return `${this.baseUrl}/productConfigs/${productId}/schemas/${schemaId}/fields`;
  }

  save(productId: number, schemaId: number, field: Field) {
    return this.http.post(`${this.fieldsUrl(productId, schemaId)}`, field);
  }

  edit(productId: number, schemaId: number, field: Field) {
    return this.http.put(
      `${this.fieldsUrl(productId, schemaId)}/${field.id}`,
      field
    );
  }

  delete(
    productId: number,
    schemaId: number,
    id: number
  ): Observable<Response> {
    return this.http.delete<any>(
      `${this.fieldsUrl(productId, schemaId)}/${id}`
    );
  }
}
