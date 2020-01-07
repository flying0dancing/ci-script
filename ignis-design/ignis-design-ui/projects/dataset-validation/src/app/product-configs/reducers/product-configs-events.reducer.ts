import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import {
  ProductActions,
  ProductActionTypes,
  ValidatePipelineTaskUpdate
} from '../actions/product-configs.actions';
import {
  PipelineTask,
  ProductConfigTaskList
} from '../interfaces/product-validation.interface';

export const ADAPTER: EntityAdapter<
  ProductConfigTaskList
> = createEntityAdapter<ProductConfigTaskList>({
  selectId: productValidationEvents => productValidationEvents.productId
});

export const INITIAL_STATE: EntityState<
  ProductConfigTaskList
> = ADAPTER.getInitialState();

function updatePipelineTaskState(
  state = INITIAL_STATE,
  action: ValidatePipelineTaskUpdate
) {
  const pipelineTasksCopy = {
    ...state.entities[action.productId].pipelineTasks
  };
  const newTaskState: PipelineTask = {
    ...pipelineTasksCopy[action.event.pipelineId],
    message: action.event.message,
    status: action.event.status,
    tasks: action.event.tasks
  };

  pipelineTasksCopy[action.event.pipelineId] = newTaskState;
  return pipelineTasksCopy;
}

export function reducer(
  state = INITIAL_STATE,
  action: ProductActions
): EntityState<ProductConfigTaskList> {
  switch (action.type) {
    case ProductActionTypes.GetTaskListSuccess:
      return ADAPTER.upsertOne(action.taskList, state);

    case ProductActionTypes.ValidatePipelineTaskUpdate:
      return ADAPTER.updateOne(
        {
          id: action.productId,
          changes: { pipelineTasks: updatePipelineTaskState(state, action) }
        },
        state
      );

    default:
      return state;
  }
}
