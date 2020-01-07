import {
  INITIAL_RESPONSE_STATE,
  ResponseState,
  toErrorState,
  toLoadingState,
  toSuccessState
} from '../../core/reducers/reducers-utility.service';
import { PipelineStepFormActions, PipelineStepFormActionTypes } from '../actions/pipeline-step-form.actions';
import { UpdatePipelineError } from '../interfaces/pipeline-step.interface';

export interface PipelineFormState extends ResponseState {
  updateError: UpdatePipelineError;
}

const emptyUpdatePipelineError: UpdatePipelineError = {
  pipelineNotFoundError: null,
  stepExecutionResult: {
    errors: [],
    selectsExecutionErrors: {
      individualErrors: [],
      transformationParseErrors: []
    }
  }
};

export const INITIAL_PIPELINE_FORM_RESPONSE_STATE: PipelineFormState = {
  ...INITIAL_RESPONSE_STATE,
  updateError: {
    pipelineNotFoundError: null,
    stepExecutionResult: {
      errors: [],
      selectsExecutionErrors: {
        individualErrors: [],
        transformationParseErrors: []
      }
    }
  }
};

export function pipelineStepFormReducer(
  state: PipelineFormState = INITIAL_PIPELINE_FORM_RESPONSE_STATE,
  action: PipelineStepFormActions
): PipelineFormState {
  switch (action.type) {
    case PipelineStepFormActionTypes.Post:
    case PipelineStepFormActionTypes.Update:
    case PipelineStepFormActionTypes.Delete:
      return {
        ...toLoadingState(state),
        updateError: emptyUpdatePipelineError
      };

    case PipelineStepFormActionTypes.PostSuccessful:
    case PipelineStepFormActionTypes.UpdateSuccessful:
    case PipelineStepFormActionTypes.DeleteSuccessful:
    case PipelineStepFormActionTypes.Reset:
      return { ...toSuccessState(state), updateError: emptyUpdatePipelineError };

    case PipelineStepFormActionTypes.PostFailed:
    case PipelineStepFormActionTypes.UpdateFailed:
      return toPipelineFormErrorState(state, action.updatePipelineError);
    case PipelineStepFormActionTypes.DeleteFail:
      return { ...toErrorState(state, action.errorResponse), updateError: emptyUpdatePipelineError };

    default:
      return state;
  }
}


export function toPipelineFormErrorState(
  state = INITIAL_RESPONSE_STATE,
  errorResponse: UpdatePipelineError
): PipelineFormState {

  if (Array.isArray(errorResponse)) {
    return {
      ...state,
      loading: false,
      loaded: true,
      updateError: {
        ...emptyUpdatePipelineError,
        unexpectedError: errorResponse
      }
    };
  }

  return {
    ...state,
    loading: false,
    loaded: true,
    updateError: errorResponse
  };
}
