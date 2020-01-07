import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ColDef, GridApi, GridOptions, GridReadyEvent } from 'ag-grid';
import { DeleteButtonRendererComponent } from '../../core/grid/components/delete-button-renderer.component';
import { createDefaultGridOptions, createDefaultTextFilterOptions } from '../../core/grid/grid.functions';
import { DeleteRendererParams } from '../../core/grid/interfaces/delete-params.interface';
import * as pipelineSteps from '../../pipelines/actions/pipeline-step-form.actions';
import { Schema } from '../../schemas/interfaces/schema.interface';
import { PipelineStep, TransformationType } from '../interfaces/pipeline-step.interface';
import { EditPipelineStepButtonRendererComponent } from './edit-pipeline-step-button-renderer.component';

@Component({
  selector: 'dv-pipeline-steps-grid',
  templateUrl: './pipeline-steps-grid.component.html'
})
export class PipelineStepsGridComponent implements OnChanges, OnInit {
  @Input() productId: number;
  @Input() pipelineId: number;
  @Input() pipelineSteps: PipelineStep[] = [];
  @Input() loading: boolean;
  @Input() schemas: Schema[];

  keyedSchemas: { [key: number]: Schema };

  quickFilterForm = this.formBuilder.group({
    quickFilter: null
  });

  gridOptions: GridOptions = {
    ...createDefaultGridOptions(),
    suppressCellSelection: true,
    paginationPageSize: 12,
    onGridReady: (readyEvent: GridReadyEvent) => this.onGridReady(readyEvent)
  };

  gridApi: GridApi;

  columnDefs: ColDef[] = [
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Name',
      field: 'name',
      cellRendererFramework: EditPipelineStepButtonRendererComponent
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Type',
      field: 'type'
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Input Schemas',
      valueGetter: params => PipelineStepsGridComponent.getInputSchemas(params, this.keyedSchemas),
    },
    {
      ...createDefaultTextFilterOptions(),
      headerName: 'Output Schemas',
      valueGetter: params => !!this.keyedSchemas[params.data.schemaOutId]
        ? this.keyedSchemas[params.data.schemaOutId].displayName
        : ''
    },
    {
      cellRendererFramework: DeleteButtonRendererComponent,
      cellRendererParams: {
        title: 'Are you sure you want to delete this pipeline step?',
        fieldNameForMessage: 'name',
        gridDeleteAction: new pipelineSteps.Delete(0, this.pipelineId)
      } as DeleteRendererParams,
      suppressResize: true,
      suppressMenu: true,
      width: 40
    }
  ];


  constructor(
    private formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    this.quickFilterForm.controls.quickFilter.valueChanges.subscribe(
      quickFilter => {
        setTimeout(() => {
          this.gridApi.setQuickFilter(quickFilter);
        }, 200);
      }
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.schemas) {
      const schemas: Schema[] = changes.schemas.currentValue;
      const keyedSchemas = {};
      schemas.forEach(schema => keyedSchemas[schema.id] = schema);
      this.keyedSchemas = keyedSchemas;

      if (!!this.gridApi) {
        this.gridApi.redrawRows();
      }
    }

    if (this.loading) {
      this.gridApi.showLoadingOverlay();
    } else {
      if (this.pipelineSteps.length === 0 && this.gridApi) {
        this.gridApi.showNoRowsOverlay();
      }
    }
  }


  private onGridReady(readyEvent: GridReadyEvent) {
    readyEvent.api.sizeColumnsToFit();
    this.gridApi = readyEvent.api;

    if (this.loading) {
      readyEvent.api.showLoadingOverlay();
    } else {
      if (this.pipelineSteps.length === 0) {
        readyEvent.api.showNoRowsOverlay();
      }
    }
  }

  private static getInputSchemas(params: any, schemas: { [key: number]: Schema }): string {
    const type: TransformationType = params.data.type

    switch (type) {
      case TransformationType.MAP:
      case TransformationType.AGGREGATION:
      case TransformationType.WINDOW:
        const schema = schemas[params.data.schemaInId];
        return !!schema ? schema.displayName : '';

      case TransformationType.JOIN:
        const joinStep = params.data;
        const inputSchemas = new Set([]);

        joinStep.joins.forEach(join => {
          const leftSchema = schemas[join.leftSchemaId];
          const rightSchema = schemas[join.rightSchemaId];
          if (!!rightSchema) {
            inputSchemas.add(rightSchema.displayName);
          }
          if (!!leftSchema) {
            inputSchemas.add(leftSchema.displayName);
          }
        });

        return Array.from(inputSchemas).reduce((previousValue, currentValue, index) => {
          if (index === 0) {
            return currentValue;
          }
          return previousValue + ',\n ' + currentValue;
        }, '');

      case TransformationType.UNION:
        const unionStep = params.data;
        const unionInputs = [];
        Object.keys(unionStep.unions)
          .forEach(schemaId => {
            const unionSchema: Schema = schemas[schemaId];
            if (!!unionSchema) {
              unionInputs.push(unionSchema.displayName);
            }
          });

        return unionInputs.reduce((previousValue, currentValue, index) => {
          if (index === 0) {
            return currentValue;
          }
          return previousValue + ',\n ' + currentValue;
        }, '');

      default:
        return type;
    }
  }
}
