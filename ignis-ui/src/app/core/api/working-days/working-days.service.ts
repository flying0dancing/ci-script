import {
  DayOfWeek,
  ProductWorkingDay
} from "@/core/api/working-days/working-days.interface";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

@Injectable()
export class WorkingDaysService {
  constructor(private http: HttpClient) {}

  static generateGetUrl(): string {
    return `${environment.api.externalRoot}/workingDays`;
  }

  static generateUpdateUrl(productId: number): string {
    return `${environment.api.externalRoot}/productConfigs/${productId}/workingDays`;
  }

  getAll(): Observable<ProductWorkingDay[]> {
    return this.http.get<ProductWorkingDay[]>(
      `${WorkingDaysService.generateGetUrl()}`
    );
  }

  update(
    productId: number,
    workingDays: DayOfWeek[]
  ): Observable<ProductWorkingDay[]> {
    const req: string[] = workingDays.map(value => value.toString());

    return this.http.put<ProductWorkingDay[]>(
      WorkingDaysService.generateUpdateUrl(productId),
      req
    );
  }
}
