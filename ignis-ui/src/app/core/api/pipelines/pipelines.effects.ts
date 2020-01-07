import { PipelinesService } from "@/core/api/pipelines/pipelines.service";
import { Injectable } from "@angular/core";
import { Actions, Effect, ofType } from "@ngrx/effects";
import { Action } from "@ngrx/store";
import { Observable, of } from "rxjs";
import { catchError, filter, map, switchMap, takeUntil } from "rxjs/operators";
import * as PipelinesActions from "./pipelines.actions";

@Injectable()
export class PipelinesEffects {
  @Effect() get$: Observable<Action> = this.actions$.pipe(
    ofType<PipelinesActions.Get>(PipelinesActions.GET),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<PipelinesActions.Empty>(PipelinesActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.get().pipe(
        map(
          pipelines =>
            new PipelinesActions.GetSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              pipelines
            })
        ),
        catchError(() =>
          of(
            new PipelinesActions.GetFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getDownstreams$: Observable<Action> = this.actions$.pipe(
    ofType<PipelinesActions.GetDownstreams>(PipelinesActions.GET_DOWNSTREAMS),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<PipelinesActions.Empty>(PipelinesActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.getDownstreams().pipe(
        map(
          pipelineDownstreams =>
            new PipelinesActions.GetDownstreamsSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              pipelineDownstreams
            })
        ),
        catchError(() =>
          of(
            new PipelinesActions.GetDownstreamsFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getRequiredSchemas$: Observable<Action> = this.actions$.pipe(
    ofType<PipelinesActions.GetRequiredSchemas>(
      PipelinesActions.GET_REQUIRED_SCHEMAS
    ),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<PipelinesActions.Empty>(PipelinesActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      return this.service.getRequiredSchemas(action.payload.pipelineId).pipe(
        map(
          requiredSchemas =>
            new PipelinesActions.GetRequiredSchemasSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              requiredSchemas: requiredSchemas
            })
        ),
        catchError(() =>
          of(
            new PipelinesActions.GetRequiredSchemasFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  @Effect() getInvocations$: Observable<Action> = this.actions$.pipe(
    ofType<PipelinesActions.GetInvocations>(PipelinesActions.GET_INVOCATIONS),
    switchMap(action => {
      const emptyWithMatchingKey$ = this.actions$.pipe(
        ofType<PipelinesActions.Empty>(PipelinesActions.EMPTY),
        filter(
          emptyAction =>
            emptyAction.payload.reducerMapKey === action.payload.reducerMapKey
        )
      );

      const pipelineInvocations$ = action.payload.jobExecutionId
        ? this.service.getInvocationsByJobId(action.payload.jobExecutionId)
        : this.service.getInvocations();

      return pipelineInvocations$.pipe(
        map(
          pipelineInvocations =>
            new PipelinesActions.GetInvocationsSuccess({
              reducerMapKey: action.payload.reducerMapKey,
              pipelineInvocations
            })
        ),
        catchError(() =>
          of(
            new PipelinesActions.GetInvocationsFail({
              reducerMapKey: action.payload.reducerMapKey
            })
          )
        ),
        takeUntil(emptyWithMatchingKey$)
      );
    })
  );

  constructor(private service: PipelinesService, private actions$: Actions) {}
}
