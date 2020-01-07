import * as RequestParamUtils from './request-param.utils';

describe('Request Param Utils', () => {
  describe('toParamString', () => {
    it('should return empty string if no params', () => {
      expect(new RequestParamUtils.RequestParamBuilder().toParamString()).toEqual('');
    });

    it('should return multiple params', () => {
      const paramString = new RequestParamUtils.RequestParamBuilder()
        .param('size', 10)
        .param('page', 1)
        .toParamString();

      expect(paramString).toEqual('?size=10&page=1');
    });

  });
});
