import { CalendarHoliday } from "@/core/api/calendar/calendar.interface";
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { Moment } from "moment";

@Component({
  selector: "app-calendar-form",
  templateUrl: "./calendar-form.component.html",
  styleUrls: ["./calendar-form.component.scss"]
})
export class CalendarFormComponent implements OnChanges {
  @Input()
  selectedDate: Moment;

  @Input()
  existingHoliday: CalendarHoliday;

  @Input()
  loading: boolean;

  @Output()
  submit: EventEmitter<HolidayForm> = new EventEmitter();

  @Output()
  deleteHoliday: EventEmitter<number> = new EventEmitter();

  public calendarForm: FormGroup = this.fb.group({
    name: [!!this.existingHoliday ? this.existingHoliday.name : null, []],
    date: [{ value: this.selectedDate, disabled: true }, [Validators.required]]
  });
  submitted = false;
  deleted = false;

  constructor(private fb: FormBuilder) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.selectedDate) {
      this.calendarForm.get("date").setValue(changes.selectedDate.currentValue);
      this.submitted = false;
      this.deleted = false;
      this.calendarForm.get("name").setValue(null);
    }

    if (changes.existingHoliday) {
      const holiday = changes.existingHoliday.currentValue;
      this.calendarForm.get("name").setValue(!!holiday ? holiday.name : null);
    }
  }

  public onSubmit() {
    if (this.calendarForm.valid) {
      const date: Moment = this.calendarForm.get("date").value;
      this.submit.emit({
        name: this.calendarForm.get("name").value,
        date: date.format("YYYY-MM-DD"),
        id: !!this.existingHoliday ? this.existingHoliday.id : null,
        type: "holidayForm"
      });
      this.submitted = true;
    }
  }

  delete() {
    this.deleteHoliday.emit(this.existingHoliday.id);
    this.submitted = true;
    this.deleted = true;
  }

  get displayText() {
    if (this.submitted && this.deleted) {
      return "Deleted Holiday";
    }

    return !!this.existingHoliday ? "Update Holiday" : "Create Holiday";
  }
}

export interface HolidayForm {
  id?: number;
  name: string;
  date: string;
  type: "holidayForm";
}
