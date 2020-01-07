import {
  areEqualLocalDates,
  compareDates,
  compareDatesByYearMonthDate,
  DateSortOrder
} from './date-utils';

describe('DateUtils', () => {
  describe('compareDates', () => {
    it('should return `0` if the dates and times are the same', () => {
      const dateA = new Date('2018-10-26T19:30:00.000Z');
      const dateB = new Date('2018-10-26T19:30:00.000Z');

      expect(compareDates(dateA, dateB)).toBe(0);
    });

    it(`should return \`-1\` if the first date is earlier than the second date and the sort
      order is ascending`, () => {
      const dateA = new Date('2000-10-26T19:30:00.000Z');
      const dateB = new Date('2018-10-26T19:30:00.000Z');

      expect(compareDates(dateA, dateB)).toBe(-1);
    });

    it(`should return \`1\` if the first date is earlier than the second date and the sort
      order is descending`, () => {
      const dateA = new Date('2000-10-26T19:30:00.000Z');
      const dateB = new Date('2018-10-26T19:30:00.000Z');

      expect(compareDates(dateA, dateB, DateSortOrder.Desc)).toBe(1);
    });

    it(`should return \`1\` if the first date is later than the second date and the sort
      order is ascending`, () => {
      const dateA = new Date('2018-10-26T19:30:00.000Z');
      const dateB = new Date('2000-10-26T19:30:00.000Z');

      expect(compareDates(dateA, dateB)).toBe(1);
    });

    it(`should return \`-1\` if the first date is later than the second date and the sort
      order is ascending`, () => {
      const dateA = new Date('2018-10-26T19:30:00.000Z');
      const dateB = new Date('2000-10-26T19:30:00.000Z');

      expect(compareDates(dateA, dateB, DateSortOrder.Desc)).toBe(-1);
    });
  });

  describe('compareDatesByYearMonthDate', () => {
    it('should return `0` if the dates are the same (ignoring time)', () => {
      const dateA = new Date('2018-10-26T19:30:00.000Z');
      const dateB = new Date('2018-10-26T05:30:00.000Z'); // same date (different time)

      expect(compareDatesByYearMonthDate(dateA, dateB)).toBe(0);
      expect(
        compareDatesByYearMonthDate(dateA, dateB, DateSortOrder.Desc)
      ).toBe(0);
    });
  });

  describe('areEqualLocalDates', () => {
    it('should return true when unix times are the same (ignoring time)', () => {
      expect(areEqualLocalDates('2018-10-26', '2018-10-26T05:30:00.000Z')).toBe(
        true
      );
    });

    it('should return false when unix times are not the same (ignoring time)', () => {
      expect(
        areEqualLocalDates(
          '2018-10-27T19:30:00.000Z',
          '2018-10-26T19:30:00.000Z'
        )
      ).toBe(false);
    });
  });
});
