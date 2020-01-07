import { Component, Input } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Schema } from '../../../../schemas';
import { addMissingOrderToSelects, PipelineMapStep, Select, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';

export interface MapFormInterface {
  selects: Select[];
  filter: string;
  schemaIn: Schema;
  schemaOut: Schema;
}

export const emptyMapForm: MapFormInterface = {
  selects: [],
  filter: '',
  schemaIn: null,
  schemaOut: null
};

@Component({
  selector: 'dv-map-step-sub-form',
  styleUrls: ['../pipeline-step-root.component.scss'],
  templateUrl: './pipeline-map-step-sub-form.component.html',
  providers: subformComponentProviders(PipelineMapStepSubFormComponent)
})
export class PipelineMapStepSubFormComponent extends NgxSubFormRemapComponent<MapFormInterface, MapFormInterface> {
  @Input()
  schemas: Schema[];

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  schemaDisplayNameFunction: (schema: Schema) => string = schema => schema.displayName + ' v' + schema.majorVersion;

  protected transformToFormGroup(obj: MapFormInterface): MapFormInterface {
    return obj;
  }

  protected transformFromFormGroup(formValue: MapFormInterface): MapFormInterface {
    return formValue;
  }

  protected getFormControls(): Controls<MapFormInterface> {
    return {
      selects: new FormControl(),
      filter: new FormControl(),
      schemaIn: new FormControl(null, Validators.required),
      schemaOut: new FormControl(null, Validators.required),
    };
  }

  resetSchemaErrors(): void {}

  schemaOutChange() {
    this.resetSchemaErrors();
  }

  public static convertMapComponent(obj: PipelineMapStep, schemaIdMap): MapFormInterface {
    return {
      selects: addMissingOrderToSelects(obj.selects),
      filter: obj.filters && obj.filters.length > 0 ? obj.filters[0] : '',
      schemaIn: schemaIdMap[obj.schemaInId],
      schemaOut: schemaIdMap[obj.schemaOutId]
    };
  }

  schemaInValueChanged(schema: Schema) {
    this.formGroupControls.schemaIn.setValue(schema);
    this.resetSchemaErrors();
  }

  schemaOutValueChanged(schema: Schema) {
    this.formGroupControls.schemaOut.setValue(schema);
    this.resetSchemaErrors();
  }
}
