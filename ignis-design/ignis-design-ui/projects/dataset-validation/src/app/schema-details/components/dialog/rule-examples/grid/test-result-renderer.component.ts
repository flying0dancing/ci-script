import { Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { AgRendererComponent } from 'ag-grid-angular';
import { TestResult } from '../../../../../schemas';

const ERROR_PREFIX = 'Unexpected Error while running test';
const FIELD_ERROR_SUFFIX = 'Check field cells for error details';

@Component({
  selector: 'dv-test-result',
  templateUrl: './test-result-renderer.component.html',
  styleUrls: ['./test-result-renderer.component.scss']
})
export class TestResultRendererComponent implements AgRendererComponent {
  actualResult: TestResult;
  expectedResult: TestResult;
  unexpectedErrorTooltip = `${ERROR_PREFIX}. ${FIELD_ERROR_SUFFIX}`;

  agInit(params: ICellRendererParams): void {
    this.actualResult = params.value;
    this.expectedResult = params.data ? params.data.expectedResult : null;

    if (params.data && params.data.unexpectedError) {
      this.unexpectedErrorTooltip = `${ERROR_PREFIX}: ${params.data.unexpectedError}`;
    }
  }

  refresh(params: any): boolean {
    return false;
  }
}
