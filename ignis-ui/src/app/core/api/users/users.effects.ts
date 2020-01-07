import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action } from "@ngrx/store";
import { Observable, of } from "rxjs";

import { catchError, filter, map, switchMap, takeUntil } from "rxjs/operators";
import { AuthService } from "../auth/auth.service";
import * as UsersActions from "./users.actions";

import { UsersService } from "./users.service";

@Injectable()
export class UsersEffects {
  @Effect() getUsers$: Observable<Action> = this.actions$.pipe(
    ofType<UsersActions.Get>(UsersActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<UsersActions.Empty>(UsersActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.usersService.getUsers().pipe(
        map(
          users =>
            new UsersActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              collection: users
            })
        ),
        catchError(() =>
          of(
            new UsersActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getUserByName$: Observable<Action> = this.actions$.pipe(
    ofType<UsersActions.GetCurrentUser>(UsersActions.GET_CURRENT_USER),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<UsersActions.Empty>(UsersActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.usersService.getUserByName().pipe(
        map(
          currentUser =>
            new UsersActions.GetCurrentUserSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              currentUser
            })
        ),
        catchError(() =>
          of(
            new UsersActions.GetCurrentUserFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() createUser$: Observable<Action> = this.actions$.pipe(
    ofType<UsersActions.Post>(UsersActions.POST),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<UsersActions.Empty>(UsersActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.authService
        .createUser(action.payload.username, action.payload.password)
        .pipe(
          map(
            () =>
              new UsersActions.PostSuccess({
                reducerMapKey: action.payload.reducerMapKey
              })
          ),
          catchError(error =>
            of(
              new UsersActions.PostFail({
                reducerMapKey: action.payload.reducerMapKey
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  @Effect() changePassword$: Observable<Action> = this.actions$.pipe(
    ofType<UsersActions.ChangePassword>(UsersActions.CHANGE_PASSWORD),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<UsersActions.Empty>(UsersActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.usersService
        .changePassword(action.payload.oldPassword, action.payload.newPassword)
        .pipe(
          map(
            () =>
              new UsersActions.ChangePasswordSuccess({
                reducerMapKey: action.payload.reducerMapKey
              })
          ),
          catchError(error =>
            of(
              new UsersActions.ChangePasswordFail({
                reducerMapKey: action.payload.reducerMapKey,
                statusCode: error
              })
            )
          ),
          takeUntil(emptyWithMatchingKey$)
        );
    })
  );

  constructor(
    private usersService: UsersService,
    private authService: AuthService,
    private actions$: Actions
  ) {}
}
