import { HttpErrorResponse } from '@angular/common/http';
import * as ruleFormActions from './rule-form.actions';

describe('RuleFormActions', () => {
  let postFailedAction: ruleFormActions.PostFailed;

  describe('PostFailed', () => {
    it('should construct ErrorResponse with errors when httpError is a business error/s', () => {
      const error = { error: [{ errorMessage: 'm', errorCode: 'e' }] };
      postFailedAction = new ruleFormActions.PostFailed(
        new HttpErrorResponse(error)
      );

      expect(postFailedAction.errorResponse.errors).toEqual([
        { errorMessage: 'm', errorCode: 'e' }
      ]);
    });
  });
});
