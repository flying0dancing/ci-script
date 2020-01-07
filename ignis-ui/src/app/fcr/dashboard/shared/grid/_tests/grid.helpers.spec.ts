import { copySelectedRow } from "../grid.helpers";

describe("Grid Helpers", () => {
  describe("copySelectedRow", () => {
    it("should copy the selected row to the clipboard", () => {
      const params = {
        node: {
          setSelected: value => true
        }
      };

      const api = {
        copySelectedRowsToClipboard: withHeaders => true
      };

      spyOn(api, "copySelectedRowsToClipboard");
      copySelectedRow(params, api, true);
      expect(api.copySelectedRowsToClipboard).toHaveBeenCalledTimes(1);
    });
  });
});
