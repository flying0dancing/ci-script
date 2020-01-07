import { Component, Input } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Schema, schemaVersionDisplay } from '../../../../schemas';
import { addMissingOrderToSelects, PipelineWindowStep, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';
import { MapFormInterface } from '../map/pipeline-map-step-sub-form.component';

@Component({
  selector: 'dv-window-step-sub-form',
  styleUrls: ['../pipeline-step-root.component.scss'],
  templateUrl: './pipeline-window-step-sub-form.component.html',
  providers: subformComponentProviders(PipelineWindowStepSubFormComponent)
})
export class PipelineWindowStepSubFormComponent extends NgxSubFormRemapComponent<MapFormInterface,
  MapFormInterface> {
  @Input()
  schemas: Schema[];

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  schemaDisplayNameFunction = schemaVersionDisplay;

  protected transformToFormGroup(obj: MapFormInterface): MapFormInterface {
    return obj;
  }

  protected transformFromFormGroup(
    formValue: MapFormInterface
  ): MapFormInterface {
    return formValue;
  }

  protected getFormControls(): Controls<MapFormInterface> {
    return {
      selects: new FormControl(),
      filter: new FormControl(),
      schemaIn: new FormControl(null, Validators.required),
      schemaOut: new FormControl(null, Validators.required)
    };
  }

  resetSchemaErrors(): void {}

  schemaOutChange() {
    this.resetSchemaErrors();
  }

  public static convertWindowComponent(
    obj: PipelineWindowStep,
    schemaIdMap
  ): MapFormInterface {
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
