import { Component } from '@angular/core';
import { IHeaderAngularComp } from 'ag-grid-angular';
import { Field } from '../../../../../schemas';

@Component({
  templateUrl: './context-field-header-renderer.component.html',
  styleUrls: ['./context-field-header-renderer.component.scss']
})
export class ContextFieldHeaderRendererComponent implements IHeaderAngularComp {
  field: Field;
  hover = false;

  agInit(params): void {
    this.field = params.field;
  }
}
