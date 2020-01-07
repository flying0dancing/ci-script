import * as CalendarActions from "@/core/api/calendar/calendar.actions";
import { NAMESPACE as CALENDAR_NAMESPACE } from "@/core/api/calendar/calendar.actions";
import { CalendarHoliday } from "@/core/api/calendar/calendar.interface";
import * as CalendarSelectors from "@/core/api/calendar/calendars.selectors";
import * as ProductActions from "@/core/api/products/products.actions";
import { Product } from "@/core/api/products/products.interfaces";
import { NAMESPACE as PRODUCT_NAMESPACE } from "@/core/api/products/products.reducer";

import * as ProductSelectors from "@/core/api/products/products.selectors";
import * as WorkingDaySelectors from "@/core/api/working-days/working-day.selectors";
import * as WorkingDayActions from "@/core/api/working-days/working-days.actions";
import { NAMESPACE as WORKING_DAY_NAMESPACE } from "@/core/api/working-days/working-days.actions";
import {
  DayOfWeek,
  ProductWorkingDay
} from "@/core/api/working-days/working-days.interface";
import { HolidayForm } from "@/fcr/shared/calendar/calendar-form.component";
import { ISO_DATE_FORMATS } from "@/shared/utilities/date.utilities";
import { Component, Inject, OnDestroy, OnInit } from "@angular/core";
import {
  MAT_DIALOG_DATA,
  MatCalendarCellCssClasses,
  MatDialogRef
} from "@angular/material";
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS } from "@angular/material-moment-adapter";
import { MatSelectChange } from "@angular/material/typings/select";
import { Store } from "@ngrx/store";
import * as moment from "moment";
import { BehaviorSubject, Observable, Subject, Subscription } from "rxjs";
import { map, withLatestFrom } from "rxjs/operators";

@Component({
  selector: "app-calendar-dialog",
  templateUrl: "./calendar-dialog.component.html",
  styleUrls: ["./calendar-dialog.component.scss"],
  providers: [
    {
      provide: MAT_MOMENT_DATE_ADAPTER_OPTIONS,
      useValue: { useUtc: true }
    }
  ]
})
export class CalendarDialogComponent implements OnInit, OnDestroy {
  products: Product[] = [];
  holidays: CalendarHoliday[] = [];
  workingDays: DayOfWeek[] = [];
  keyedHolidays: { [key: string]: CalendarHoliday } = {};

  selectedProduct: Product;
  selectedHoliday: CalendarHoliday;

  selectedProduct$: Subject<Product> = new Subject();
  renderCalendar: Subject<boolean> = new BehaviorSubject(true);

  products$: Observable<Product[]> = this.store.select(
    ProductSelectors.getProductsCollectionFactory(PRODUCT_NAMESPACE)
  );
  holidays$: Observable<{
    [key: string]: CalendarHoliday[];
  }> = this.store.select(CalendarSelectors.getCalendars());
  workingDays$: Observable<{
    [key: string]: ProductWorkingDay[];
  }> = this.store.select(WorkingDaySelectors.getWorkingDays());

  createHolidayLoading$: Observable<boolean> = this.store
    .select(
      CalendarSelectors.getCalendarsCreateLoadingStateFactory(
        CALENDAR_NAMESPACE
      )
    )
    .pipe(
      withLatestFrom(
        this.store.select(
          CalendarSelectors.getCalendarsUpdateLoadingStateFactory(
            CALENDAR_NAMESPACE
          )
        ),
        this.store.select(
          CalendarSelectors.getCalendarsDeleteLoadingStateFactory(
            CALENDAR_NAMESPACE
          )
        )
      ),
      map(
        ([createLoading, updateLoading, deleteLoading]) =>
          createLoading || updateLoading || deleteLoading
      )
    );

  workingDaysLoading$: Observable<boolean> = this.store
    .select(
      WorkingDaySelectors.getWorkingDaysGetLoadingStateFactory(
        WORKING_DAY_NAMESPACE
      )
    )
    .pipe(
      withLatestFrom(
        this.store.select(
          WorkingDaySelectors.getWorkingDaysUpdateLoadingStateFactory(
            WORKING_DAY_NAMESPACE
          )
        )
      ),
      map(([getLoading, updateLoading]) => getLoading || updateLoading)
    );

  subscriptions: Subscription[] = [];
  selectedDate: any;

  constructor(
    public dialogRef: MatDialogRef<CalendarDialogComponent>,
    private store: Store<any>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  ngOnInit(): void {
    this.store.dispatch(
      new ProductActions.Get({ reducerMapKey: PRODUCT_NAMESPACE })
    );
    this.store.dispatch(
      new CalendarActions.Get({ reducerMapKey: CALENDAR_NAMESPACE })
    );
    this.store.dispatch(
      new WorkingDayActions.Get({ reducerMapKey: WORKING_DAY_NAMESPACE })
    );

    this.subscriptions.push(
      this.products$.subscribe(products => (this.products = products))
    );

    const productChangeSub = this.selectedProduct$
      .pipe(withLatestFrom(this.holidays$, this.workingDays$))
      .subscribe(value => {
        const product = value[0];
        const holidays = value[1];
        const workingDays = value[2];
        if (!!product) {
          this.handleHolidaysChange(holidays[product.name]);
          this.handleWorkingDaysChanges(workingDays[product.name]);
        }
      });

    const holidayChangeSub = this.holidays$.subscribe(holidays => {
      if (!!this.selectedProduct) {
        this.handleHolidaysChange(holidays[this.selectedProduct.name]);
      }
    });

    this.subscriptions.push(productChangeSub);
    this.subscriptions.push(holidayChangeSub);
  }

  private handleHolidaysChange(holidays: CalendarHoliday[]) {
    this.keyedHolidays = {};
    this.holidays = !!holidays ? holidays : [];
    this.renderCalendar.next(false);

    this.holidays.forEach(holiday => {
      const holidayAsMoment = moment(holiday.date);
      const holidayKey = holidayAsMoment.format(
        ISO_DATE_FORMATS.parse.dateInput
      );
      this.keyedHolidays[holidayKey] = holiday;
    });
    setTimeout(() => this.renderCalendar.next(true), 0);
  }

  private handleWorkingDaysChanges(productWorkingDays: ProductWorkingDay[]) {
    this.workingDays = !!productWorkingDays
      ? productWorkingDays.map(value => value.dayOfWeek)
      : [];
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  productSelection($event: MatSelectChange) {
    this.selectedProduct = $event.value;
    this.selectedProduct$.next($event.value);
  }

  onDateChanged($event: any) {
    this.selectedHoliday = this.keyedHolidays[
      $event.format(ISO_DATE_FORMATS.parse.dateInput)
    ];
  }

  dateClassFunction(): (date: Date) => MatCalendarCellCssClasses {
    return date => {
      const holidayKey = moment(date).format(ISO_DATE_FORMATS.parse.dateInput);
      return !!this.keyedHolidays && !!this.keyedHolidays[holidayKey]
        ? "holiday-exists"
        : "";
    };
  }

  submitHolidayChange($event: HolidayForm) {
    if ($event.type === "holidayForm" && !$event.id) {
      this.store.dispatch(
        new CalendarActions.Create({
          reducerMapKey: CALENDAR_NAMESPACE,
          productName: this.selectedProduct.name,
          date: $event.date,
          name: $event.name
        })
      );
    } else if ($event.type === "holidayForm" && !!$event.id) {
      this.store.dispatch(
        new CalendarActions.Update({
          reducerMapKey: CALENDAR_NAMESPACE,
          calendarId: $event.id,
          date: $event.date,
          name: $event.name
        })
      );
    }
  }

  deleteHoliday($event: number) {
    this.store.dispatch(
      new CalendarActions.Delete({
        reducerMapKey: CALENDAR_NAMESPACE,
        calendarId: $event,
        productName: this.selectedProduct.name
      })
    );
  }

  saveWorkingDays($event: DayOfWeek[]) {
    this.store.dispatch(
      new WorkingDayActions.Update({
        reducerMapKey: WORKING_DAY_NAMESPACE,
        workingDays: $event,
        productName: this.selectedProduct.name,
        productId: this.selectedProduct.id
      })
    );
  }
}
