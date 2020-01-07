import { Component, Input } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { Schema, SchemaMetadata, schemaVersionDisplay } from '../../../../schemas';
import {
  Join,
  JoinField,
  JoinFieldRequest,
  JoinRequest,
  PipelineJoinStep,
  Select,
  SelectsExecutionErrors
} from '../../../interfaces/pipeline-step.interface';

export interface JoinFormInterface {
  schemaOut: Schema;
  selects: Select[];
  joins: Join[];
}

@Component({
  selector: 'dv-join-step-sub-form',
  templateUrl: './pipeline-join-step-sub-form.component.html',
  providers: subformComponentProviders(PipelineJoinStepSubFormComponent)
})
export class PipelineJoinStepSubFormComponent extends NgxSubFormRemapComponent<JoinFormInterface,
  JoinFormInterface> {
  @Input()
  schemas: Schema[];

  @Input()
  selectExecutionErrors: SelectsExecutionErrors = null;

  joinSchemas: Schema[] = [];

  joinSchemasMetadata: SchemaMetadata[] = [];

  schemaDisplayNameFunction: (schema: Schema) => string = schemaVersionDisplay;

  protected transformToFormGroup(obj: JoinFormInterface): JoinFormInterface {
    return obj;
  }

  protected transformFromFormGroup(
    formValue: JoinFormInterface
  ): JoinFormInterface {
    return formValue;
  }

  protected getFormControls(): Controls<JoinFormInterface> {
    return {
      schemaOut: new FormControl(null, Validators.required),
      selects: new FormControl(null, Validators.required),
      joins: new FormControl(null, Validators.required)
    };
  }

  resetSchemaErrors(): void {}

  schemaOutChange(schemaOut) {
    this.resetSchemaErrors();
  }

  public static convertJoinComponent(
    obj: PipelineJoinStep,
    schemaIdMap
  ): JoinFormInterface {
    function convertToJoins(joinRequests: JoinRequest[]) {
      return joinRequests.map(joinRequest => convertToJoin(joinRequest));
    }

    function convertToJoin(joinRequest: JoinRequest): Join {
      const leftSchema = schemaIdMap[joinRequest.leftSchemaId];
      const rightSchema = schemaIdMap[joinRequest.rightSchemaId];
      return {
        id: joinRequest.id,
        leftSchema: leftSchema,
        rightSchema: rightSchema,
        joinFields: convertToJoinFields(
          leftSchema,
          rightSchema,
          joinRequest.joinFields
        ),
        joinType: joinRequest.joinType
      };
    }

    function convertToJoinFields(
      leftSchema: Schema,
      rightSchema: Schema,
      joinFields: JoinFieldRequest[]
    ): JoinField[] {
      return joinFields
        ? joinFields.map(fieldId =>
          convertToJoinField(leftSchema, rightSchema, fieldId)
        )
        : [];
    }

    function convertToJoinField(
      leftSchema: Schema,
      rightSchema: Schema,
      joinFieldRequest: JoinFieldRequest
    ): JoinField {
      return {
        leftField: leftSchema.fields.find(
          field => field.id === joinFieldRequest.leftFieldId
        ),
        rightField: rightSchema.fields.find(
          field => field.id === joinFieldRequest.rightFieldId
        )
      };
    }

    return {
      selects: obj.selects,
      schemaOut: schemaIdMap[obj.schemaOutId],
      joins: convertToJoins(obj.joins)
    };
  }

  joinSchemaSelected(schemasSelected: Schema[]) {
    this.joinSchemas = [...schemasSelected];
  }

  joinSchemaMetadataSelected(schemasMetadataSelected: SchemaMetadata[]) {
    this.joinSchemasMetadata = [...schemasMetadataSelected];
  }

  schemaOutValueChanged($event: Schema) {
    this.formGroupControls.schemaOut.setValue($event);
  }
}
