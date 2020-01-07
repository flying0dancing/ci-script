import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { FormArray, FormControl, Validators } from '@angular/forms';
import {
  ArrayPropertyKey,
  ArrayPropertyValue,
  Controls,
  NgxFormWithArrayControls,
  NgxSubFormRemapComponent,
  subformComponentProviders
} from 'ngx-sub-form';
import { Subscription } from 'rxjs';
import { Schema, SchemaMetadata } from '../../../../schemas';
import { Join, JoinType, SelectsExecutionErrors } from '../../../interfaces/pipeline-step.interface';

interface PipelineJoinsForm {
  joins: Join[];
}

@Component({
  selector: 'dv-joins-form',
  templateUrl: './pipeline-joins-form.component.html',
  styleUrls: ['./pipeline-joins-form.component.scss'],
  providers: subformComponentProviders(PipelineJoinsFormComponent)
})
export class PipelineJoinsFormComponent
  extends NgxSubFormRemapComponent<Join[], PipelineJoinsForm>
  implements NgxFormWithArrayControls<PipelineJoinsForm>, OnInit, OnDestroy {
  @Input()
  schemas: Schema[];

  @Output()
  joinSchemas: EventEmitter<Schema[]> = new EventEmitter();

  @Output()
  joinSchemasMetadata: EventEmitter<SchemaMetadata[]> = new EventEmitter();

  joinTypes: JoinType[] = [JoinType.LEFT, JoinType.FULLOUTER, JoinType.INNER];

  private joinsChangeSubscription: Subscription;

  ngOnInit(): void {
    this.formGroupControls.joins.push(
      this.createFormArrayControl('joins', {
        id: null,
        leftSchema: null,
        rightSchema: null,
        joinFields: null,
        joinType: null
      })
    );

    this.emitCurrentSelectedJoinSchemas(this.formGroupControls.joins.value);

    this.joinsChangeSubscription = this.formGroupControls.joins.valueChanges.subscribe(
      joinsForm => {
        this.emitCurrentSelectedJoinSchemas(joinsForm);
      }
    );
  }

  ngOnDestroy(): void {
    super.ngOnDestroy();
    this.joinsChangeSubscription.unsubscribe();
  }

  protected getFormControls(): Controls<PipelineJoinsForm> {
    return {
      joins: new FormArray([])
    };
  }

  protected transformToFormGroup(obj: Join[] | null): PipelineJoinsForm {
    return {
      joins: obj ? obj : []
    };
  }

  protected transformFromFormGroup(
    formValue: PipelineJoinsForm
  ): Join[] | null {
    return formValue.joins;
  }

  public removeJoin(index: number): void {
    this.formGroupControls.joins.removeAt(index);
  }

  public addJoin(): void {
    this.formGroupControls.joins.push(
      this.createFormArrayControl('joins', {
        id: null,
        leftSchema: null,
        rightSchema: null,
        joinFields: null,
        joinType: null
      })
    );
  }

  public createFormArrayControl(
    key: ArrayPropertyKey<PipelineJoinsForm> | undefined,
    value: ArrayPropertyValue<PipelineJoinsForm>
  ): FormControl {
    if (key === 'joins') {
      return new FormControl(value, [Validators.required]);
    } else {
      return new FormControl(value);
    }
  }

  private emitCurrentSelectedJoinSchemas(joins: Join[]) {
    const joinSchemas = [];
    const joinSchemasMetadata = [];
    joins.forEach(join => {
      if (
        join &&
        join.leftSchema &&
        joinSchemas.indexOf(join.leftSchema) === -1
      ) {
        joinSchemas.push(join.leftSchema);
        joinSchemasMetadata.push({
          physicalTableName: join.leftSchema.physicalTableName,
          joinSchemaType: 'left',
          aggregationSchemaType: '',
          aggregationGroupingFieldNames: '',
          fieldNames: join.leftSchema.fields.map(field => field.name)
        });
      }
      if (
        join &&
        join.rightSchema &&
        joinSchemas.indexOf(join.rightSchema) === -1
      ) {
        joinSchemas.push(join.rightSchema);
        joinSchemasMetadata.push({
          physicalTableName: join.rightSchema.physicalTableName,
          joinSchemaType: 'right',
          aggregationSchemaType: '',
          aggregationGroupingFieldNames: '',
          fieldNames: join.rightSchema.fields.map(field => field.name)
        });
      }
    });

    this.joinSchemas.emit(joinSchemas);
    this.joinSchemasMetadata.emit(joinSchemasMetadata);
  }
}
