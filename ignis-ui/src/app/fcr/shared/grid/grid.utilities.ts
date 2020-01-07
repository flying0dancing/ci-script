export class GridUtilities {
  public static serverSideFilterParams: {} = {
    newRowsAction: 'keep',
    clearButton: true,
    applyButton: true,
    suppressAndOrCondition: true,
    textFormatter: function(r) {
      if (r == null) return null;
      return r;
    }
  };

}
