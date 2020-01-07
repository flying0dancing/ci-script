import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { select, Store } from '@ngrx/store';
import * as moment from 'moment';
import { Moment } from 'moment';
import { GET_SCHEMAS_ERROR, GET_SCHEMAS_LOADING, getSchema } from 'projects/dataset-validation/src/app/schemas/reducers/schemas.selectors';
import { Observable, Subscription } from 'rxjs';
import { filter, map, switchMap, withLatestFrom } from 'rxjs/operators';
import { ErrorDialogComponent } from '../../core/dialogs/components/error-dialog.component';
import { ShortcutsService } from '../../core/shortcuts/shortcuts.service';
import { ISO_DATE_FORMATS } from '../../core/utilities';
import { Field, GetOne, RuleRequest, RuleResponse, Schema, UpdateRequest } from '../../schemas';
import { SchemasRepository } from '../../schemas/services/schemas.repository';
import * as fieldFormActions from '../actions/field-form.actions';
import * as ruleFormActions from '../actions/rule-form.actions';
import { isFieldEdited, isFieldNew, isNewOrEdited } from '../functions/schema-details.functions';
import { EditFieldEvent, EditRuleEvent } from '../interfaces/events.interfaces';
import { UpdateSchemaRequest } from '../interfaces/update-schema-request.interface';
import { FieldEventService } from '../service/field-event.service';
import { RuleEventService } from '../service/rule-event.service';
import { FieldFormComponent } from './contained/field-form.component';
import { RuleFormComponent } from './contained/rule-form.component';

@Component({
  selector: 'dv-schema-details-container',
  templateUrl: './schema-details-container.component.html',
  styleUrls: ['./schema-details-container.component.scss']
})
export class SchemaDetailsContainerComponent implements OnInit, OnDestroy {
  @ViewChild('ruleForm', { static: true }) ruleFormComponent: RuleFormComponent;
  @ViewChild('fieldForm', { static: true })
  fieldFormComponent: FieldFormComponent;

  sidenavOpened: boolean;
  showFieldForm: boolean;

  productId: number;
  schema: Schema;
  displayName: string;
  rules: RuleResponse[];
  fields: Field[];
  orderedFieldIds: number[];

  loading$: Observable<boolean> = this.store.select(GET_SCHEMAS_LOADING);
  loaded$: Observable<boolean> = this.loading$.pipe(
    filter(loading => !loading)
  );

  idParam$: Observable<number> = this.route.paramMap.pipe(
    map(params => parseInt(params.get('id'), 10))
  );

  productName$: Observable<string> = this.route.queryParamMap.pipe(
    map(queryParams => queryParams.get('productName'))
  );

  productIdParam$: Observable<number> = this.route.paramMap.pipe(
    map(params => parseInt(params.get('productId'), 10))
  );

  schema$: Observable<Schema> = this.loaded$.pipe(
    withLatestFrom(this.idParam$),
    switchMap(([loaded, id]) => this.store.pipe(select(getSchema(id))))
  );

  editDisplayName: (
    displayName: string
  ) => void = this.submitEditDisplayName.bind(this);
  editPhysicalTableName: (
    physicalName: string
  ) => void = this.submitEditPhysicalTableName.bind(this);
  editStartDate: (date: Moment) => void = this.submitEditStartDate.bind(this);
  editEndDate: (date: Moment) => void = this.submitEditEndDate.bind(this);

  private editRuleSubscription: Subscription;
  private editFieldSubscription: Subscription;

  constructor(
    private route: ActivatedRoute,
    private store: Store<any>,
    private fb: FormBuilder,
    private ruleEventService: RuleEventService,
    private fieldEventService: FieldEventService,
    private schemaRepository: SchemasRepository,
    private dialog: MatDialog,
    private shortcutsService: ShortcutsService
  ) {
    this.store.pipe(select(GET_SCHEMAS_ERROR)).subscribe(isError => {
      if (isError) {
        this.openErrorDialog(
          'Something went wrong while processing your request'
        );
      }
    });

    this.editRuleSubscription = this.ruleEventService.editEvent.subscribe(
      (editEvent: EditRuleEvent) => {
        this.editRule(editEvent);
      }
    );

    this.editFieldSubscription = this.fieldEventService.editEvent.subscribe(
      (editEvent: EditFieldEvent) => {
        this.editField(editEvent);
      }
    );
  }

  ngOnInit() {
    this.setupShortcuts();

    this.idParam$
      .pipe(
        withLatestFrom(this.productIdParam$),
        map(([id, productId]) => {
          this.store.dispatch(new GetOne(productId, id));
          return productId;
        })
      )
      .subscribe(productId => (this.productId = productId));

    this.schema$.pipe(filter(schema => !!schema)).subscribe(schema => {
      this.schema = schema;
      this.displayName = schema.displayName;
      this.fields = schema.fields;
      this.orderedFieldIds = schema.fields
        .map(field => field.id)
        .sort((n1, n2) => n1 - n2);
      this.rules = schema.validationRules;
    });
  }

  ngOnDestroy() {
    this.editRuleSubscription.unsubscribe();
    this.editFieldSubscription.unsubscribe();
  }

  dispatchPostRule(ruleRequest: RuleRequest) {
    if (isNewOrEdited(ruleRequest, this.rules)) {
      this.store.dispatch(
        new ruleFormActions.Post(this.productId, this.schema.id, ruleRequest)
      );
    }
  }

  dispatchPostField(field: Field) {
    if (isFieldNew(field, this.fields)) {
      this.store.dispatch(
        new fieldFormActions.Post(this.productId, this.schema.id, field)
      );
      return;
    }

    if (isFieldEdited(field, this.fields)) {
      this.store.dispatch(
        new fieldFormActions.Edit(this.productId, this.schema.id, field)
      );
    }
  }

  addRule() {
    this.showFieldForm = false;
    this.ruleFormComponent.resetForm();
    this.sidenavOpened = true;
  }

  addField() {
    this.showFieldForm = true;
    this.fieldFormComponent.resetForm();
    this.sidenavOpened = true;
  }

  closeSidenav() {
    this.sidenavOpened = false;
  }

  openSidenav() {
    this.sidenavOpened = true;
  }

  private submitEditDisplayName(displayName: string) {
    const updateSchemaRequest = new UpdateSchemaRequest(
      displayName,
      this.schema.physicalTableName,
      this.schema.startDate,
      this.schema.endDate
    );
    this.store.dispatch(
      new UpdateRequest(this.productId, this.schema.id, updateSchemaRequest)
    );
  }

  private submitEditPhysicalTableName(physicalTableName: string) {
    const updateSchemaRequest = new UpdateSchemaRequest(
      this.schema.displayName,
      physicalTableName.toUpperCase(),
      this.schema.startDate,
      this.schema.endDate
    );

    this.store.dispatch(
      new UpdateRequest(this.productId, this.schema.id, updateSchemaRequest)
    );
  }

  private submitEditStartDate(startDate: moment.Moment) {
    const startDateString = startDate.format(ISO_DATE_FORMATS.parse.dateInput);
    const updateSchemaRequest = new UpdateSchemaRequest(
      this.schema.displayName,
      this.schema.physicalTableName,
      startDateString,
      this.schema.endDate
    );
    this.store.dispatch(
      new UpdateRequest(this.productId, this.schema.id, updateSchemaRequest)
    );
  }

  private submitEditEndDate(endDate: moment.Moment) {
    const endDateString = endDate
      ? endDate.format(ISO_DATE_FORMATS.parse.dateInput)
      : null;
    const updateSchemaRequest = new UpdateSchemaRequest(
      this.schema.displayName,
      this.schema.physicalTableName,
      this.schema.startDate,
      endDateString
    );
    this.store.dispatch(
      new UpdateRequest(this.productId, this.schema.id, updateSchemaRequest)
    );
  }

  private setupShortcuts() {
    this.shortcutsService.keepCheatSheetShortcutsOnly();
  }

  private openErrorDialog(message: string): void {
    let dialogRef = this.dialog.open(ErrorDialogComponent, {
      width: '250px',
      data: { message: message }
    });

    dialogRef.afterClosed().subscribe(() => {
      dialogRef = undefined;
    });
  }

  private editRule(editEvent: EditRuleEvent) {
    const ruleToEdit: RuleResponse = this.rules.find(
      (existingRule: RuleResponse) => existingRule.id === editEvent.id
    );

    this.showFieldForm = false;
    this.openSidenav();
    this.ruleFormComponent.edit(ruleToEdit);
  }

  private editField(editEvent: EditFieldEvent) {
    const fieldToEdit: Field = this.fields.find(
      existingField => existingField.id === editEvent.id
    );

    this.showFieldForm = true;
    this.openSidenav();
    this.fieldFormComponent.edit(fieldToEdit);
  }

  createExampleCsv() {
    window.open(this.createExampleCsvUrl());
  }

  createExampleCsvUrl() {
    return this.schemaRepository.createExampleCsvUrl(this.productId, this.schema.id);
  }
}
