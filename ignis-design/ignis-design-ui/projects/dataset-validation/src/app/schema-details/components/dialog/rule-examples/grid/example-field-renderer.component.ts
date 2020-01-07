import { Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { AgRendererComponent } from 'ag-grid-angular';
import { ExampleField } from '../../../../interfaces/rule-example.interface';

@Component({
  selector: 'dv-example-field-renderer',
  templateUrl: './example-field-renderer.component.html'
})
export class ExampleFieldRendererComponent implements AgRendererComponent {
  exampleField: ExampleField;

  agInit(params: ICellRendererParams): void {
    this.exampleField = params.value;
  }

  refresh(): boolean {
    return false;
  }
}
