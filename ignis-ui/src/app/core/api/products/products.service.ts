import { Identifiable } from "@/core/api/common/identifiable.interface";
import {
  Product,
  UploadResponse
} from "@/core/api/products/products.interfaces";
import { HttpClient, HttpEvent, HttpRequest } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";
import { map } from "rxjs/operators";

@Injectable()
export class ProductsService {
  constructor(private http: HttpClient) {}

  static generateGetUrl(): string {
    return `${environment.api.externalRoot}/productConfigs`;
  }

  static generateUploadUrl(): string {
    return `${this.generateGetUrl()}/file`;
  }

  get(): Observable<Product[]> {
    return this.http.get<Product[]>(`${ProductsService.generateGetUrl()}`);
  }

  upload(formData: FormData): Observable<HttpEvent<UploadResponse>> {
    const request = new HttpRequest(
      "POST",
      ProductsService.generateUploadUrl(),
      formData,
      {
        reportProgress: true,
        withCredentials: true
      }
    );

    return this.http.request<UploadResponse>(request);
  }

  delete(id: number): Observable<number> {
    return this.http
      .delete<Identifiable>(`${ProductsService.generateGetUrl()}/${id}`)
      .pipe(map((identifiable: Identifiable) => identifiable.id));
  }
}
