import { CalendarHoliday } from "@/core/api/calendar/calendar.interface";
import { Action } from "@ngrx/store";

export const NAMESPACE = "calendars";
export const GET = "calendars/get";
export const GET_SUCCESS = "calendars/get-success";
export const GET_FAIL = "calendars/get-fail";
export const CREATE = "calendars/create";
export const CREATE_SUCCESS = "calendars/create-success";
export const CREATE_FAIL = "calendars/create-fail";
export const UPDATE = "calendars/update";
export const UPDATE_SUCCESS = "calendars/update-success";
export const UPDATE_FAIL = "calendars/update-fail";
export const DELETE = "calendars/delete";
export const DELETE_SUCCESS = "calendars/delete-success";
export const DELETE_FAIL = "calendars/delete-fail";
export const EMPTY = "calendars/empty";

export class Get implements Action {
  readonly type = GET;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class GetSuccess implements Action {
  readonly type = GET_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; calendars: CalendarHoliday[] }
  ) {}
}

export class GetFail implements Action {
  readonly type = GET_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Create implements Action {
  readonly type = CREATE;

  constructor(
    public payload: {
      reducerMapKey: string;
      productName: string;
      date: string;
      name: string;
    }
  ) {}
}

export class CreateSuccess implements Action {
  readonly type = CREATE_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; calendar: CalendarHoliday }
  ) {}
}

export class CreateFail implements Action {
  readonly type = CREATE_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Update implements Action {
  readonly type = UPDATE;

  constructor(
    public payload: {
      reducerMapKey: string;
      calendarId: number;
      date: string;
      name: string;
    }
  ) {}
}

export class UpdateSuccess implements Action {
  readonly type = UPDATE_SUCCESS;

  constructor(
    public payload: { reducerMapKey: string; calendar: CalendarHoliday }
  ) {}
}

export class UpdateFail implements Action {
  readonly type = UPDATE_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Delete implements Action {
  readonly type = DELETE;

  constructor(
    public payload: {
      reducerMapKey: string;
      calendarId: number;
      productName: string;
    }
  ) {}
}

export class DeleteSuccess implements Action {
  readonly type = DELETE_SUCCESS;

  constructor(
    public payload: {
      reducerMapKey: string;
      calendarId: number;
      productName: string;
    }
  ) {}
}

export class DeleteFail implements Action {
  readonly type = DELETE_FAIL;

  constructor(public payload: { reducerMapKey: string }) {}
}

export class Empty implements Action {
  readonly type = EMPTY;

  constructor(public payload: { reducerMapKey: string }) {}
}

export type Types =
  | Get
  | GetSuccess
  | GetFail
  | Create
  | CreateSuccess
  | CreateFail
  | Update
  | UpdateSuccess
  | UpdateFail
  | Delete
  | DeleteSuccess
  | DeleteFail
  | Empty;
