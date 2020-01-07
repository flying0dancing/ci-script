import { Action } from '@ngrx/store';
import { ICellRendererParams } from 'ag-grid';

export interface GridDeleteAction extends Action {
  id: number;
}

export type GridDeleteCallback = (params: ICellRendererParams) => any;
export type Visible = (params: ICellRendererParams) => boolean;

export interface DeleteRendererParams {
  title: string;
  fieldNameForMessage: string;
  gridDeleteAction?: GridDeleteAction;
  gridDeleteCallback?: GridDeleteCallback;
  visible?: Visible;
}
