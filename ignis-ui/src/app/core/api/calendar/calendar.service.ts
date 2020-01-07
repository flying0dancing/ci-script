import {
  CalendarHoliday,
  CreateHolidayRequest
} from "@/core/api/calendar/calendar.interface";
import { Identifiable } from "@/core/api/common/identifiable.interface";
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { environment } from "@env/environment";
import { Observable } from "rxjs";

@Injectable()
export class CalendarService {
  constructor(private http: HttpClient) {}

  static generateGetUrl(): string {
    return `${environment.api.externalRoot}/calendars`;
  }

  getAll(): Observable<CalendarHoliday[]> {
    return this.http.get<CalendarHoliday[]>(
      `${CalendarService.generateGetUrl()}`
    );
  }

  create(
    productName: string,
    holiday: string,
    name: string
  ): Observable<CalendarHoliday> {
    const req: CreateHolidayRequest = {
      date: holiday,
      productName,
      name
    };

    return this.http.post<CalendarHoliday>(
      `${CalendarService.generateGetUrl()}`,
      req
    );
  }

  update(
    calendarId: number,
    holiday: string,
    name: string
  ): Observable<CalendarHoliday> {
    const req: CreateHolidayRequest = {
      date: holiday,
      name
    };

    return this.http.put<CalendarHoliday>(
      `${CalendarService.generateGetUrl()}/${calendarId}`,
      req
    );
  }

  delete(calendarId: number): Observable<Identifiable> {
    return this.http.delete<Identifiable>(
      `${CalendarService.generateGetUrl()}/${calendarId}`
    );
  }
}
