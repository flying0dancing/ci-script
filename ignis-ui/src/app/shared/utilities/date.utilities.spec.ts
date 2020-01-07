import { ISO_DATE_FORMATS } from "@/shared/utilities/date.utilities";
import * as moment from "moment";

describe("Moment js", () => {
  it("should parse timezone and adjust", () => {
    const dateTime = moment("2011-12-03T10:15:30+01:00");

    expect(
      dateTime.utc().format(ISO_DATE_FORMATS.display.dateTimeInput)
    ).toEqual("2011-12-03 - 09:15:30");
  });
});
