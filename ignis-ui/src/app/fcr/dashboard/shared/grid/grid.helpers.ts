export const copySelectedRow = (params, api, withHeaders) => {
  const { node } = params;

  node.setSelected(true);
  api.copySelectedRowsToClipboard(withHeaders);
  node.setSelected(false);
};
