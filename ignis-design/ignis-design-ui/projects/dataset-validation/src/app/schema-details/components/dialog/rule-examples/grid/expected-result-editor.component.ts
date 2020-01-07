import { Component } from '@angular/core';
import { ICellEditorParams } from 'ag-grid';
import { AgEditorComponent } from 'ag-grid-angular';
import { TestResult } from '../../../../../schemas';

@Component({
  selector: 'dv-expected-result',
  templateUrl: './expected-result-editor.component.html',
  styleUrls: ['./expected-result-editor.component.scss']
})
export class ExpectedResultEditorComponent implements AgEditorComponent {
  expectedResult: TestResult;

  agInit(params: ICellEditorParams): void {
    this.expectedResult = params.value;
  }

  getValue(): any {
    return this.expectedResult;
  }
}
