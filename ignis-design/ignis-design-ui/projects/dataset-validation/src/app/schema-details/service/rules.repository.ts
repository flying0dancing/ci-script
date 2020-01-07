import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentService } from '../../../environments/environment.service';
import { RuleExample } from '../interfaces/rule-example.interface';

@Injectable({ providedIn: 'root' })
export class RulesRepository {
  private readonly baseUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.baseUrl = `${this.environmentService.env.api.root}`;
  }

  rulesUrl(productId: number, schemaId: number) {
    return `${this.baseUrl}/productConfigs/${productId}/schemas/${schemaId}/rules`;
  }

  delete(
    productId: number,
    schemaId: number,
    id: number
  ): Observable<Response> {
    return this.http.delete<any>(`${this.rulesUrl(productId, schemaId)}/${id}`);
  }

  getExamples(
    productId: number,
    schemaId: number,
    id: number
  ): Observable<RuleExample[]> {
    return this.http.get<RuleExample[]>(
      `${this.rulesUrl(productId, schemaId)}/${id}/examples`
    );
  }
}
