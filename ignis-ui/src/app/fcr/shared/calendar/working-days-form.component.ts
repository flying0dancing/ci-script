import { DayOfWeek } from "@/core/api/working-days/working-days.interface";
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from "@angular/core";
import { FormBuilder, FormGroup } from "@angular/forms";

@Component({
  selector: "app-working-days-form",
  templateUrl: "./working-days-form.component.html",
  styleUrls: ["./working-days-form.component.scss"]
})
export class WorkingDaysFormComponent implements OnChanges, OnInit {
  @Input()
  workingDays: DayOfWeek[] = [];

  @Input()
  loading: boolean;

  submitted = false;

  @Output()
  public workingDaysSubmit: EventEmitter<DayOfWeek[]> = new EventEmitter();

  public workingDaysForm: FormGroup = this.fb.group({
    monday: [],
    tuesday: [],
    wednesday: [],
    thursday: [],
    friday: [],
    saturday: [],
    sunday: []
  });

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.workingDaysForm.valueChanges.subscribe(() => (this.submitted = false));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.workingDays) {
      this.submitted = false;
      this.resetWorkingDays();

      if (this.updateWorkingDays(changes.workingDays.currentValue)) {
        this.setDefaultWorkingDays();
      }
    }
  }

  private updateWorkingDays(daysOfWeek: DayOfWeek[]) {
    let noDaysSet = true;
    daysOfWeek.forEach(day => {
      switch (day) {
        case DayOfWeek.MONDAY:
          this.workingDaysForm.get('monday').setValue(true);
          noDaysSet = false;
          break;
        case DayOfWeek.TUESDAY:
          this.workingDaysForm.get('tuesday').setValue(true);
          noDaysSet = false;
          break;
        case DayOfWeek.WEDNESDAY:
          this.workingDaysForm.get('wednesday').setValue(true);
          noDaysSet = false;
          break;
        case DayOfWeek.THURSDAY:
          this.workingDaysForm.get('thursday').setValue(true);
          noDaysSet = false;
          break;
        case DayOfWeek.FRIDAY:
          this.workingDaysForm.get('friday').setValue(true);
          noDaysSet = false;
          break;
        case DayOfWeek.SATURDAY:
          this.workingDaysForm.get('saturday').setValue(true);
          noDaysSet = false;
          break;
        case DayOfWeek.SUNDAY:
          this.workingDaysForm.get('sunday').setValue(true);
          noDaysSet = false;
      }
    });
    return noDaysSet;
  }

  private setDefaultWorkingDays() {
    this.workingDaysForm.setValue({
      monday: true,
      tuesday: true,
      wednesday: true,
      thursday: true,
      friday: true,
      saturday: false,
      sunday: false
    });
  }

  private resetWorkingDays() {
    this.workingDaysForm.setValue({
      monday: false,
      tuesday: false,
      wednesday: false,
      thursday: false,
      friday: false,
      saturday: false,
      sunday: false
    });
  }

  save() {
    const workingDays = [];
    if (this.workingDaysForm.get("monday").value) {
      workingDays.push(DayOfWeek.MONDAY);
    }
    if (this.workingDaysForm.get("tuesday").value) {
      workingDays.push(DayOfWeek.TUESDAY);
    }
    if (this.workingDaysForm.get("wednesday").value) {
      workingDays.push(DayOfWeek.WEDNESDAY);
    }
    if (this.workingDaysForm.get("thursday").value) {
      workingDays.push(DayOfWeek.THURSDAY);
    }
    if (this.workingDaysForm.get("friday").value) {
      workingDays.push(DayOfWeek.FRIDAY);
    }
    if (this.workingDaysForm.get("saturday").value) {
      workingDays.push(DayOfWeek.SATURDAY);
    }
    if (this.workingDaysForm.get("sunday").value) {
      workingDays.push(DayOfWeek.SUNDAY);
    }

    this.workingDaysSubmit.emit(workingDays);
    this.submitted = true;
  }
}
