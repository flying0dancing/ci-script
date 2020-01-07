import { Component, Input, OnInit } from '@angular/core';
import { ColDef, GridOptions, GridReadyEvent } from 'ag-grid';
import 'ag-grid-enterprise';
import { DateRendererComponent } from '../../../core/grid/components/date-renderer.component';
import { DeleteButtonRendererComponent } from '../../../core/grid/components/delete-button-renderer.component';
import {
  createDefaultDateFilterOptions,
  createDefaultGridOptions,
  createDefaultTextFilterOptions
} from '../../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../../core/grid/interfaces/delete-params.interface';

import { RuleResponse } from '../../../schemas';
import { EditRuleButtonRendererComponent } from '../../../schemas/components/contained/edit-rule-button-renderer.component';
import { Delete } from '../../actions/rule-form.actions';

@Component({
  selector: 'dv-rule-grid',
  templateUrl: './rule.grid.component.html',
  styleUrls: ['../schema-details-container.component.scss']
})
export class RuleGridComponent implements OnInit {
  @Input() rules: RuleResponse[] = [];
  @Input() loading: boolean;
  @Input() schemaId: number;
  @Input() productId: number;

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    suppressRowClickSelection: true,
    suppressCellSelection: true,
    paginationPageSize: 8,
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent)
  };

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Regulator Reference',
      cellRendererFramework: EditRuleButtonRendererComponent,
      field: 'ruleId',
      tooltipField: 'ruleId',
      width: 220,
      pinned: true
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Name',
      field: 'name',
      tooltipField: 'name',
      width: 300
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Description',
      field: 'description',
      tooltipField: 'description',
      width: 530
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Version',
      field: 'version',
      width: 120
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Rule Type',
      field: 'validationRuleType',
      width: 120
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Rule Severity',
      field: 'validationRuleSeverity',
      width: 140
    },
    {
      ...createDefaultDateFilterOptions(),
      headerName: 'Start Date',
      field: 'startDate',
      cellRendererFramework: DateRendererComponent,
      width: 160
    },
    {
      ...createDefaultDateFilterOptions(),
      headerName: 'End Date',
      field: 'endDate',
      cellRendererFramework: DateRendererComponent,
      width: 160
    }
  ];

  private onGridReady(readyEvent: GridReadyEvent) {
    if (this.loading) {
      readyEvent.api.showLoadingOverlay();
    } else {
      if (this.rules.length === 0) {
        readyEvent.api.showNoRowsOverlay();
      }
    }
  }

  ngOnInit(): void {
    this.columnDefs.push({
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this rule?',
        fieldNameForMessage: 'name',
        gridDeleteAction: new Delete(0, this.schemaId, this.productId)
      } as DeleteRendererParams,
      suppressMenu: true,
      suppressResize: true,
      width: 65
    });
  }
}
