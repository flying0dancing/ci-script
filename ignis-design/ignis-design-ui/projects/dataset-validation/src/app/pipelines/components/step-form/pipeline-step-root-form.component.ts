import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import { AbstractControl, FormControl } from '@angular/forms';
import { Controls, FormUpdate, NgxRootFormComponent } from 'ngx-sub-form';
import { Subscription } from 'rxjs';
import { mapToObject } from '../../../core/utilities/map.utilities';
import { Schema } from '../../../schemas';
import {
  isEmpty,
  Join,
  JoinField,
  JoinFieldRequest,
  JoinRequest,
  PipelineAggregationStep,
  PipelineJoinStep,
  PipelineMapStep,
  PipelineScriptletStep,
  PipelineStep,
  PipelineUnionMapStep,
  PipelineWindowStep,
  SelectsExecutionErrors,
  toTopLevelFormErrors,
  TransformationType,
  Union,
  UpdatePipelineError
} from '../../interfaces/pipeline-step.interface';
import { PipelineStepsRepository } from '../../services/pipeline-steps.repository';
import { StepSelectEditorService } from '../../services/step-select-editor.service';
import {
  AggregationFormInterface,
  emptyAggregationForm,
  PipelineAggregationStepSubFormComponent
} from './aggregation/pipeline-aggregation-step-sub-form.component';
import { JoinFormInterface, PipelineJoinStepSubFormComponent } from './join/pipeline-join-step-sub-form.component';
import { emptyMapForm, MapFormInterface, PipelineMapStepSubFormComponent } from './map/pipeline-map-step-sub-form.component';
import {
  emptyScriptletForm,
  PipelineScriptletStepSubFormComponent,
  ScriptletFormInterface
} from './Scriptlet/pipeline-scriptlet-step-sub-form.component';
import { UnionForm } from './union/pipeline-union-form.component';
import {
  emptyUnionsMapForm,
  PipelineUnionsStepSubFormComponent,
  UnionMapFormInterface
} from './union/pipeline-unions-step-sub-form.component';
import { PipelineWindowStepSubFormComponent } from './window/pipeline-window-step-sub-form.component';

export enum Mode {
  CREATE = 'Create',
  EDIT = 'Edit'
}

export interface StepForm {
  id: number;
  name: string;
  description: string | null;
  type: TransformationType | null;
  map: MapFormInterface | null;
  aggregation: AggregationFormInterface | null;
  join: JoinFormInterface | null;
  window: MapFormInterface | null;
  union: UnionMapFormInterface | null;
  scriptlet: ScriptletFormInterface | null;
}

@Component({
  selector: 'dv-step-root-form',
  styleUrls: ['./pipeline-step-root.component.scss'],
  templateUrl: './pipeline-step-root-form.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepRootFormComponent extends NgxRootFormComponent<PipelineStep, StepForm> implements OnChanges, OnInit, OnDestroy {
  saveButtonText = 'Save';
  title = ' Pipeline Step';

  @ViewChild('firstInput', { static: true }) firstInput: ElementRef;

  @Input()
  public dataInput: PipelineStep | null | undefined;

  @Input()
  schemas: Schema[];

  @Input()
  loading = false;

  @Input()
  loaded = false;

  @Input()
  mode: Mode = Mode.CREATE;

  @Input()
  apiErrors: UpdatePipelineError = null;

  @Output()
  public dataOutput: EventEmitter<PipelineStep> = new EventEmitter();

  @Output()
  public closeSidenavOutput: EventEmitter<void> = new EventEmitter();

  public TransformationType: typeof TransformationType = TransformationType;

  private checkSyntaxSubscription: Subscription;

  constructor(private selectEventService: StepSelectEditorService,
              private pipelineStepRepository: PipelineStepsRepository) {
    super();
  }


  ngOnInit(): void {
    super.ngOnInit();
    this.checkSyntaxSubscription = this.selectEventService.checkSyntax.subscribe(check => {
      this.pipelineStepRepository.checkSyntax({
        outputFieldId: check.fieldId,
        sparkSql: check.sparkSql,
        pipelineStep: this.transformFromFormGroup(this.formGroup.value)
      }).subscribe(result => {
        this.selectEventService.checkSyntaxSuccess.emit(result);
      });
    });

    this.transformationTypeChangeClearsErrorState();
  }

  private transformationTypeChangeClearsErrorState() {
    this.formGroupControls.type.valueChanges.subscribe(change => {
      if (!!change) {
        this.apiErrors = null;
      }
    });
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    if (!!this.checkSyntaxSubscription) {
      this.checkSyntaxSubscription.unsubscribe();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.loading && !changes.loading.currentValue) {
      this.saveButtonText = 'Saved';
    }

    if (changes.apiErrors
      && changes.apiErrors.currentValue != null
      && !isEmpty(changes.apiErrors.currentValue as UpdatePipelineError)) {

      const valueBeforeError = this.dataValue;
      this.resetForm();
      this.writeValue(valueBeforeError);
      this.saveButtonText = 'Save';
    }
  }

  protected getFormControls(): Controls<StepForm> {
    return {
      id: new FormControl(),
      name: new FormControl(),
      description: new FormControl(),
      type: new FormControl(),
      map: new FormControl(),
      aggregation: new FormControl(),
      join: new FormControl(),
      window: new FormControl(),
      union: new FormControl(),
      scriptlet: new FormControl(),
    };
  }

  protected transformFromFormGroup(formValue: StepForm): PipelineStep | null {
    return convertToPipelineStep(formValue);
  }

  protected transformToFormGroup(obj: PipelineStep): StepForm {
    return convertToStepForm(this.schemas, obj);
  }

  savePipelineStep() {
    if (this.isFormValid()) {
      this.formGroup.disable();
      this.loading = true;
      this.dataOutput.emit(this.transformFromFormGroup(this.formGroup.value));
    }
  }

  get executionErrors(): SelectsExecutionErrors {
    if (!this.apiErrors || !this.apiErrors.stepExecutionResult) {
      return null;
    }

    return this.apiErrors.stepExecutionResult.selectsExecutionErrors;
  }

  isFormValid() {
    switch (this.formGroup.controls.type.value) {
      case this.TransformationType.MAP:
        return this.controlsValid(this.formGroup.controls.map);
      case this.TransformationType.JOIN:
        return this.controlsValid(this.formGroup.controls.join);
      case this.TransformationType.AGGREGATION:
        return this.controlsValid(this.formGroup.controls.aggregation);
      case this.TransformationType.WINDOW:
        return this.controlsValid(this.formGroup.controls.window);
      case this.TransformationType.UNION:
        return this.controlsValid(this.formGroup.controls.union);
      case this.TransformationType.SCRIPTLET:
        return this.controlsValid(this.formGroup.controls.scriptlet);
    }
    return false;
  }

  private controlsValid(stepTypeControl: AbstractControl) {
    return this.commonPropertiesValid()
      && stepTypeControl.valid;
  }

  private commonPropertiesValid() {
    return this.formGroupControls.name.valid
      && this.formGroupControls.description.valid
      && this.formGroupControls.type.valid;
  }

  public resetForm() {
    this.formGroup.reset({});
    this.formGroup.enable();
    this.formGroup.setErrors({});
    this.saveButtonText = 'Save';
    this.focusOnFirstInput();
  }

  private focusOnFirstInput() {
    setTimeout(() => this.firstInput.nativeElement.focus(), 200);
  }

  closeSidenav() {
    this.closeSidenavOutput.emit();
  }

  enable() {
    this.formGroup.enable();
    this.saveButtonText = 'Save';
  }

  setMode(mode: Mode) {
    this.mode = mode;
  }

  onFormUpdate(formUpdate: FormUpdate<StepForm>): void {
    if (this.isFormValid()) {
      this.setDisabledState(false);
    }
  }

  toTopLevelFormErrors(apiErrors: UpdatePipelineError) {
    return toTopLevelFormErrors(apiErrors);
  }
}

const emptyForm: StepForm = {
  id: null,
  name: null,
  description: null,
  type: null,
  map: emptyMapForm,
  aggregation: emptyAggregationForm,
  join: null,
  window: emptyMapForm,
  union: emptyUnionsMapForm,
  scriptlet: emptyScriptletForm,
};

function convertToMapStep(stepForm: StepForm): PipelineMapStep {
  return {
    id: stepForm.id,
    name: stepForm.name,
    description: stepForm.description,
    type: TransformationType.MAP,
    filters: stepForm.map && stepForm.map.filter ? [stepForm.map.filter] : [],
    selects: stepForm.map ? stepForm.map.selects : [],
    schemaInId:
      stepForm.map && stepForm.map.schemaIn ? stepForm.map.schemaIn.id : null,
    schemaOutId:
      stepForm.map && stepForm.map.schemaOut ? stepForm.map.schemaOut.id : null
  };
}

function convertToScriptletStep(stepForm: StepForm): PipelineScriptletStep {
  return {
    id: stepForm.id,
    name: stepForm.name,
    description: stepForm.description,
    type: TransformationType.SCRIPTLET,
    jarName: stepForm.scriptlet ? stepForm.scriptlet.jarName : null,
    className: stepForm.scriptlet ? stepForm.scriptlet.className : null,
    scriptletInputs: stepForm.scriptlet ? stepForm.scriptlet.scriptletInputs : [],
    schemaOutId:
      stepForm.scriptlet && stepForm.scriptlet.schemaOut ? stepForm.scriptlet.schemaOut.id : null
  };
}

function convertToAggregationStep(stepForm: StepForm): PipelineAggregationStep {
  return {
    id: stepForm.id,
    name: stepForm.name,
    description: stepForm.description,
    type: TransformationType.AGGREGATION,
    filters:
      stepForm.aggregation && stepForm.aggregation.filter
        ? [stepForm.aggregation.filter]
        : [],
    groupings: stepForm.aggregation ? stepForm.aggregation.groupings : [],
    selects: stepForm.aggregation ? stepForm.aggregation.selects : [],
    schemaInId:
      stepForm.aggregation && stepForm.aggregation.schemaIn
        ? stepForm.aggregation.schemaIn.id
        : null,
    schemaOutId:
      stepForm.aggregation && stepForm.aggregation.schemaOut
        ? stepForm.aggregation.schemaOut.id
        : null
  };
}

function convertToWindowStep(stepForm: StepForm): PipelineWindowStep {
  return {
    id: stepForm.id,
    name: stepForm.name,
    description: stepForm.description,
    type: TransformationType.WINDOW,
    selects: stepForm.window ? stepForm.window.selects : [],
    filters: stepForm.window && stepForm.window.filter ? [stepForm.window.filter] : [],
    schemaInId:
      stepForm.window && stepForm.window.schemaIn
        ? stepForm.window.schemaIn.id
        : null,
    schemaOutId:
      stepForm.window && stepForm.window.schemaOut
        ? stepForm.window.schemaOut.id
        : null
  };
}

function convertToJoinStep(stepForm: StepForm): PipelineJoinStep {
  return {
    id: stepForm.id,
    name: stepForm.name,
    description: stepForm.description,
    schemaOutId:
      stepForm.join && stepForm.join.schemaOut ? stepForm.join.schemaOut.id : null,
    selects: stepForm.join ? stepForm.join.selects : [],
    joins: stepForm.join ? convertToJoinWithIds(stepForm.join.joins) : [],
    type: TransformationType.JOIN
  };
}

function convertToJoinWithIds(joins: Join[]): JoinRequest[] {
  return joins ? joins.map(join => convertToJoinRequest(join)) : [];
}

function convertToJoinRequest(join: Join): JoinRequest {
  if (!join) {
    return null;
  }

  return {
    id: join.id,
    leftSchemaId: join.leftSchema ? join.leftSchema.id : null,
    rightSchemaId: join.rightSchema ? join.rightSchema.id : null,
    joinFields: join.joinFields ? join.joinFields.map(joinField => convertToJoinFieldRequest(joinField)) : [],
    joinType: join.joinType
  };
}

function convertToJoinFieldRequest(joinField: JoinField): JoinFieldRequest {
  if (!joinField) {
    return null;
  }
  return {
    leftFieldId: joinField.leftField ? joinField.leftField.id : null,
    rightFieldId: joinField.rightField ? joinField.rightField.id : null
  };
}

function convertUnions(unions: UnionForm[]): { [key: number]: Union } {
  const unionMapObj = new Map<number, Union>();
  unions.forEach(union => {
    unionMapObj.set(union.schemaIn.id, {
      filters: union.filter ? [union.filter] : [],
      selects: union.selects
    });
  });

  return mapToObject<Union>(unionMapObj);
}

function convertToUnionStep(stepForm: StepForm): PipelineUnionMapStep {
  return {
    id: stepForm.id,
    name: stepForm.name,
    description: stepForm.description,
    type: TransformationType.UNION,
    schemaOutId:
      stepForm.union && stepForm.union.schemaOut ? stepForm.union.schemaOut.id : null,
    unions: convertUnions(stepForm.union ? stepForm.union.unions : [])
  };
}

function convertToPipelineStep(stepForm: StepForm): PipelineStep {
  switch (stepForm.type) {
    case TransformationType.MAP:
      return convertToMapStep(stepForm);
    case TransformationType.AGGREGATION:
      return convertToAggregationStep(stepForm);
    case TransformationType.WINDOW:
      return convertToWindowStep(stepForm);
    case TransformationType.JOIN:
      return convertToJoinStep(stepForm);
    case TransformationType.UNION:
      return convertToUnionStep(stepForm);
    case TransformationType.SCRIPTLET:
      return convertToScriptletStep(stepForm);
    default:
      return null;
  }
}

function convertToStepForm(schemas: Schema[], obj: PipelineStep) {
  if (obj === null) return emptyForm;

  const schemaIdMap = {};
  schemas.forEach(schema => (schemaIdMap[schema.id] = schema));

  const common = {
    ...emptyForm,
    id: obj.id,
    name: obj.name,
    description: obj.description ? obj.description : null,
    type: obj.type
  };

  switch (obj.type) {
    case TransformationType.WINDOW:
      return { ...common, window: PipelineWindowStepSubFormComponent.convertWindowComponent(obj, schemaIdMap) };
    case TransformationType.MAP:
      return { ...common, map: PipelineMapStepSubFormComponent.convertMapComponent(obj, schemaIdMap) };
    case TransformationType.AGGREGATION:
      return { ...common, aggregation: PipelineAggregationStepSubFormComponent.convertAggregationComponent(obj, schemaIdMap) };
    case TransformationType.JOIN:
      return { ...common, join: PipelineJoinStepSubFormComponent.convertJoinComponent(obj, schemaIdMap) };
    case TransformationType.UNION:
      return { ...common, union: PipelineUnionsStepSubFormComponent.convertUnionComponent(obj, schemaIdMap) };
    case TransformationType.SCRIPTLET:
      return { ...common, scriptlet: PipelineScriptletStepSubFormComponent.convertScriptletComponent(obj, schemaIdMap) };
  }
}
