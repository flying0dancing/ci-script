import { Feature } from "@/core/api/features/features.interfaces";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

@Injectable()
export class FeaturesService {
  constructor(private http: HttpClient) {}

  static generateGetUrl(): string {
    return `${environment.api.internalRoot}/features`;
  }

  get(): Observable<Feature[]> {
    return this.http.get<Feature[]>(`${FeaturesService.generateGetUrl()}`);
  }
}
