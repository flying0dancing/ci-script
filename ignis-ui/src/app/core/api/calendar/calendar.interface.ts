export interface CalendarHoliday {
  id: number;
  product: string;
  date: string;
  name: string;
}

export interface CreateHolidayRequest {
  productName?: string;
  date: string;
  name: string;
}
