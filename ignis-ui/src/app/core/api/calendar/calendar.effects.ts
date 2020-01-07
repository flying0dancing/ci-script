import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action } from "@ngrx/store";
import { Observable, of } from "rxjs";
import { catchError, filter, map, switchMap, takeUntil } from "rxjs/operators";
import * as CalendarActions from "./calendar.actions";
import { CalendarService } from "./calendar.service";

@Injectable()
export class CalendarEffects {
  @Effect() get$: Observable<Action> = this.actions$.pipe(
    ofType<CalendarActions.Get>(CalendarActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service.getAll().pipe(
        map(
          calendars =>
            new CalendarActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              calendars
            })
        ),
        catchError(() =>
          of(
            new CalendarActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() post$: Observable<Action> = this.actions$.pipe(
    ofType<CalendarActions.Create>(CalendarActions.CREATE),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service
        .create(
          action.payload.productName,
          action.payload.date,
          action.payload.name
        )
        .pipe(
          map(
            calendar =>
              new CalendarActions.CreateSuccess({
                reducerMapKey: action.payload.reducerMapKey,
                calendar
              })
          ),
          catchError(() =>
            of(
              new CalendarActions.CreateFail({
                reducerMapKey: action.payload.reducerMapKey
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  @Effect() update: Observable<Action> = this.actions$.pipe(
    ofType<CalendarActions.Update>(CalendarActions.UPDATE),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service
        .update(
          action.payload.calendarId,
          action.payload.date,
          action.payload.name
        )
        .pipe(
          map(
            calendar =>
              new CalendarActions.UpdateSuccess({
                reducerMapKey: action.payload.reducerMapKey,
                calendar
              })
          ),
          catchError(() =>
            of(
              new CalendarActions.UpdateFail({
                reducerMapKey: action.payload.reducerMapKey
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  @Effect() delete$: Observable<Action> = this.actions$.pipe(
    ofType<CalendarActions.Delete>(CalendarActions.DELETE),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.getEmptyWithMatchingKey$(action);

      return this.service.delete(action.payload.calendarId).pipe(
        map(
          calendar =>
            new CalendarActions.DeleteSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              calendarId: calendar.id,
              productName: action.payload.productName
            })
        ),
        catchError(() =>
          of(
            new CalendarActions.DeleteFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  constructor(private service: CalendarService, private actions$: Actions) {}

  private getEmptyWithMatchingKey$(action) {
    return this.actions$.pipe(
      ofType<CalendarActions.Empty>(CalendarActions.EMPTY),
      filter(
        emptyAction =>
          emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
      )
    );
  }
}
