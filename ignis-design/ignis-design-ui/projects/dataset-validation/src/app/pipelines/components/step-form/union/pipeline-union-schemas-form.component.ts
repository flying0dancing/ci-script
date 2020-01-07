import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormControl, ValidatorFn } from '@angular/forms';
import { Controls, NgxSubFormRemapComponent, subformComponentProviders } from 'ngx-sub-form';
import { CommonValidators } from '../../../../core/forms/common.validators';
import { Schema, schemaVersionDisplay } from '../../../../schemas';

interface PipelineUnionSchemasForm {
  schemasValue: string,
  schemasArray: string[]
}

@Component({
  selector: 'dv-union-input-schemas-form',
  templateUrl: './pipeline-union-schemas-form.component.html',
  providers: subformComponentProviders(PipelineUnionSchemasFormComponent), // <-- Add this
})
export class PipelineUnionSchemasFormComponent extends NgxSubFormRemapComponent<Schema[], PipelineUnionSchemasForm> implements OnInit, OnChanges {

  @Input()
  inputSchemas: Schema[];

  @Output()
  schemaAdded: EventEmitter<Schema> = new EventEmitter();
  @Output()
  schemaRemoved: EventEmitter<Schema> = new EventEmitter();

  schemaInNames: string[];
  schemaInNameMap: {[key: string]: Schema};
  schemaInNameValidator: ValidatorFn;
  private schemaNamesSelected: string[] = [];

  ngOnInit(): void {
    this.setSchemaInProperties(this.inputSchemas);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.inputSchema) {
      this.setSchemaInProperties(changes.inputSchema.currentValue);
    }
  }

  protected transformToFormGroup(obj: Schema[] | null): PipelineUnionSchemasForm {
    this.schemaNamesSelected = obj.map(schema => this.schemaVersionName(schema));
    return {
      schemasArray: this.schemaNamesSelected,
      schemasValue: null
    };
  }

  protected transformFromFormGroup(formValue: PipelineUnionSchemasForm): Schema[] | null {
    return formValue.schemasArray.map(schemaName => this.schemaInNameMap[schemaName]);
  }

  protected getFormControls(): Controls<PipelineUnionSchemasForm> {
    return {
      schemasValue: new FormControl(null),
      schemasArray: new FormArray([])
    };
  }

  private schemaVersionName(schema: Schema) {
    return schema === null ? null : schemaVersionDisplay(schema);
  }

  private setSchemaInProperties(schemas: Schema[]): void {

    if (!!schemas) {
      this.inputSchemas = schemas;
      this.schemaInNameMap = {};

      schemas.forEach(schema => {
        this.schemaInNameMap[this.schemaVersionName(schema)] = schema;
      });

      this.schemaInNames = Object.keys(this.schemaInNameMap);
      this.schemaInNameValidator = CommonValidators.requireMatch(this.schemaInNames);
    }
  }

  public handleSchemaChanges(newValue: string[]) {
    const schemaNameMap = new Map<string, Schema>();
    this.inputSchemas.forEach(schema => schemaNameMap.set(schemaVersionDisplay(schema), schema));

    const schemasAdded = [];
    const schemasRemoved = [];

    for (const schema of newValue) {
      const indexOfSchema = this.schemaNamesSelected.indexOf(schema);
      if (indexOfSchema === -1) {
        schemasAdded.push(schema);
      }
    }

    for (const existingSchema of this.schemaNamesSelected) {
      if (newValue.indexOf(existingSchema) === -1) {
        schemasRemoved.push(existingSchema);
      }
    }

    this.schemaNamesSelected = newValue;
    schemasAdded.forEach(added => this.schemaAdded.emit(schemaNameMap.get(added)));
    schemasRemoved.forEach(removed => this.schemaRemoved.emit(schemaNameMap.get(removed)));
  }
}
