import { copySelectedRow } from "./grid.helpers";

export const DEFAULT_CONTEXT_MENU_ITEMS = (params, api) => [
  "copy",
  "copyWithHeaders",
  {
    name: "Copy Row",
    icon: '<span class="ag-icon ag-icon-copy"></span>',
    action: () => copySelectedRow(params, api, false)
  },
  {
    name: "Copy Row with Headers",
    icon: '<span class="ag-icon ag-icon-copy"></span>',
    action: () => copySelectedRow(params, api, true)
  },
  "paste"
];

export const CELL_CLASSES = {
  NUMERIC: "cell-numeric"
};
