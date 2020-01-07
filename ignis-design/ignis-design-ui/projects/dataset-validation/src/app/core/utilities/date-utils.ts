import * as moment from 'moment';

export const ISO_DATE_FORMATS = {
  parse: {
    dateInput: 'YYYY-MM-DD'
  },
  display: {
    dateInput: 'YYYY-MM-DD',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'MMMM YYYY',
    monthYearA11yLabel: 'MMMM YYYY'
  }
};

export enum DateSortOrder {
  Asc,
  Desc
}

export function compareDates(
  firstDate: Date,
  secondDate: Date,
  sortOrder = DateSortOrder.Asc
): number {
  const isAscending = sortOrder === DateSortOrder.Asc;

  if (firstDate < secondDate) {
    return isAscending ? -1 : 1;
  } else if (firstDate > secondDate) {
    return isAscending ? 1 : -1;
  } else {
    return 0;
  }
}

export function compareDatesByYearMonthDate(
  firstDate: Date,
  secondDate: Date,
  sortOrder = DateSortOrder.Asc
): number {
  firstDate.setHours(0, 0, 0, 0);
  secondDate.setHours(0, 0, 0, 0);

  return compareDates(firstDate, secondDate, sortOrder);
}

export function areEqualLocalDates(
  firstDate: string,
  secondDate: string
): boolean {
  const dateComparison = compareDatesByYearMonthDate(
    new Date(firstDate),
    new Date(secondDate)
  );
  return dateComparison === 0;
}

export function asLocalDate(unixTime: number): string {
  return moment(unixTime).format('dddd, LL');
}

export function asLocalDateTime(unixTime: number): string {
  return moment(unixTime).format('LLLL');
}
