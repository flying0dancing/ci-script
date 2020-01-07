import { ChangeDetectorRef, Component, Input, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Schema } from '../../../../schemas';
import { PipelineScriptletStep, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';
import { PipelineScriptletService } from './pipeline-scriptlet.service';

export interface ScriptletFormInterface {
  jarName: string;
  className: string;
  scriptletInputs: ScriptletInputInterface[];
  schemaOut: Schema;
}

export interface ScriptletInputInterface {
  metadataInput: string;
  schemaInId: number;
}

export const emptyScriptletForm: ScriptletFormInterface = {
  jarName: null,
  className: null,
  scriptletInputs: [],
  schemaOut: null
};

@Component({
  selector: 'dv-scriptlet-step-sub-form',
  styleUrls: ['../pipeline-step-root.component.scss'],
  templateUrl: './pipeline-scriptlet-step-sub-form.component.html',
  providers: subformComponentProviders(PipelineScriptletStepSubFormComponent)
})
export class PipelineScriptletStepSubFormComponent extends NgxSubFormRemapComponent<ScriptletFormInterface, ScriptletFormInterface> implements OnInit {

  @Input()
  schemas: Schema[];
  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  metadataInputs: string[];
  selectedScriptletInputs: ScriptletInputInterface[] = [];
  jarNames: string[];
  classNames: string[];

  constructor(
    public pipelineScriptletService: PipelineScriptletService,
    public changeDetectorRef: ChangeDetectorRef
  ) {
    super();
  }

  public static convertScriptletComponent(obj: PipelineScriptletStep, schemaIdMap): ScriptletFormInterface {
    return {
      jarName: obj.jarName,
      className: obj.className,
      scriptletInputs: obj.scriptletInputs,
      schemaOut: schemaIdMap[obj.schemaOutId]
    };
  }

  ngOnInit() {
    this.getJarNames();
  }

  getMetadataInputs(): void {
    this.pipelineScriptletService
      .getMetadataInputs(this.formGroupControls.jarName.value, this.formGroupControls.className.value)
      .subscribe(
        data => {
          this.metadataInputs = data;
          this.changeDetectorRef.detectChanges();
        },
      );
  }

  getJarNames(): void {
    this.pipelineScriptletService.getJarNames().subscribe(
      data => this.jarNames = data,
    );
  }

  schemaDisplayNameFunction: (schema: Schema) => string = schema => schema.displayName + ' v' + schema.majorVersion;

  getClassNames(): void {
    const selectedJar = this.formGroupControls.jarName.value;
    if (!selectedJar) {
      return;
    }

    this.pipelineScriptletService.getClassNames(selectedJar).subscribe(
      data => this.classNames = data,
    );
  }

  jarNameDisplayNameFunction: (jarName: string) => string = jarName => jarName;

  protected transformToFormGroup(obj: ScriptletFormInterface): ScriptletFormInterface {
    return obj;
  }

  protected transformFromFormGroup(formValue: ScriptletFormInterface): ScriptletFormInterface {
    return formValue;
  }

  classNameDisplayNameFunction: (className: string) => string = className => className;

  schemaInValueChanged(schema: Schema, metadataInp: string) {

    const scriptletInputInterface: ScriptletInputInterface = {
      metadataInput: metadataInp,
      schemaInId: schema.id
    };

    const index = this.selectedScriptletInputs.indexOf(scriptletInputInterface);
    if (index !== -1) {
      this.selectedScriptletInputs[index] = scriptletInputInterface;
    } else {
      this.selectedScriptletInputs.push(scriptletInputInterface);
    }

    this.formGroupControls.scriptletInputs.setValue(this.selectedScriptletInputs);
  }

  schemaOutValueChanged(schema: Schema) {
    this.formGroupControls.schemaOut.setValue(schema);
  }

  jarNameValueChanged(str: string) {
    this.formGroupControls.jarName.setValue(str);
    this.getClassNames();
  }

  classNameValueChanged(str: string) {
    this.formGroupControls.className.setValue(str);
    this.getMetadataInputs();
  }

  protected getFormControls(): Controls<ScriptletFormInterface> {
    return {
      jarName: new FormControl(null, Validators.required),
      className: new FormControl(null, Validators.required),
      scriptletInputs: new FormControl(null, Validators.required),
      schemaOut: new FormControl(null, Validators.required)
    };
  }
}
