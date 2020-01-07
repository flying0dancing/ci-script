import { Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { Store } from '@ngrx/store';
import { Subscription } from 'rxjs/internal/Subscription';
import { filter, map, withLatestFrom } from 'rxjs/operators';
import { CommonValidators } from '../../../core/forms/common.validators';
import { FieldValidators } from '../../../core/forms/field.validators';
import { ResponseState } from '../../../core/reducers/reducers-utility.service';
import { ShortcutsService } from '../../../core/shortcuts/shortcuts.service';
import { ISO_DATE_FORMATS } from '../../../core/utilities';
import { Field } from '../../../schemas';
import * as fieldFormActions from '../../actions/field-form.actions';
import * as fieldFormSelectors from '../../selectors/field-form.selector';

const SAVE_BUTTON_TEXT = 'Save';
const FIELD_TYPES = [
  'decimal',
  'string',
  'date',
  'int',
  'long',
  'boolean',
  'timestamp',
  'double',
  'float'
];

@Component({
  selector: 'dv-field-form',
  templateUrl: './field-form.component.html',
  styleUrls: ['./field-form.component.scss'],
  providers: [{ provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMATS }]
})
export class FieldFormComponent implements OnInit, OnDestroy {
  @Input() schemaId: number;
  @Input() productId: number;
  @Input() fields: Field[];

  @Output() submitFieldEvent: EventEmitter<Field> = new EventEmitter();
  @Output() closeSidenavEvent: EventEmitter<any> = new EventEmitter();

  @ViewChild('firstInput', { static: true }) firstInput: ElementRef;

  fieldTypes: string[] = FIELD_TYPES;

  loading$ = this.store.select(fieldFormSelectors.LOADING);
  loaded$ = this.store.select(fieldFormSelectors.LOADED);

  saving$ = this.loading$.pipe(
    withLatestFrom(this.loaded$),
    map(([loading, loaded]) => loading && !loaded)
  );
  title: string;
  saveButtonText = SAVE_BUTTON_TEXT;
  closeFieldFormShortcut: string;

  form = this.formBuilder.group({
    id: null,
    name: [null, CommonValidators.fieldNamePattern],
    type: null,
    nullable: true,
    maxLength: [null, FieldValidators.validateStringMinMax],
    minLength: [null, FieldValidators.validateStringMinMax],
    regularExpression: null,
    format: [null, FieldValidators.validateFormat],
    precision: [null, FieldValidators.validatePrecision],
    scale: [null, FieldValidators.validateScale]
  });
  idControl = this.form.get('id');
  nameControl = this.form.get('name');
  typeControl = this.form.get('type');
  nullableControl = this.form.get('nullable');
  maxLengthControl = this.form.get('maxLength');
  minLengthControl = this.form.get('minLength');
  regularExpressionControl = this.form.get('regularExpression');
  formatControl = this.form.get('format');
  precisionControl = this.form.get('precision');
  scaleControl = this.form.get('scale');

  private savedSubscription: Subscription;
  private savingSubscription: Subscription;

  constructor(
    private formBuilder: FormBuilder,
    private store: Store<ResponseState>,
    private shortcutsService: ShortcutsService
  ) {}

  ngOnInit(): void {
    this.setupShortcuts();
    this.setupOnSaving();
    this.setupOnSaved();
  }

  private setupShortcuts() {
    const closeFieldFormHotkey = this.shortcutsService.registerCloseHotkey(
      () => {
        if (this.form.disabled) {
          this.closeSidenav();
        }
      }
    );
    this.closeFieldFormShortcut = closeFieldFormHotkey.formatted[0].toUpperCase();
    this.shortcutsService.registerSaveHotkey(() => {
      if (!this.form.disabled && !this.form.invalid) {
        this.saveField();
      }
    });
  }

  private setupOnSaving(): void {
    this.savingSubscription = this.saving$
      .pipe(filter((saving: boolean) => saving))
      .subscribe(() => {
        this.saveButtonText = 'Saving';
        this.form.disable();
      });
  }

  private setupOnSaved(): void {
    this.savedSubscription = this.saving$
      .pipe(filter(saving => !saving))
      .subscribe(() => {
        if (this.isEditMode()) {
          this.title = `Edited Field: ${this.form.getRawValue().name}`;
        } else {
          this.title = `Created Field: ${this.form.getRawValue().name}`;
        }
        this.saveButtonText = 'Saved';
      });
  }

  ngOnDestroy(): void {
    this.savingSubscription.unsubscribe();
    this.savedSubscription.unsubscribe();
  }

  resetForm(): void {
    this.store.dispatch(new fieldFormActions.Reset());

    this.form.enable();
    this.form.reset({ nullable: true });
    this.form.setErrors({});

    this.title = 'Create Field';
    this.saveButtonText = SAVE_BUTTON_TEXT;

    this.focusOnFirstInput();
  }

  private focusOnFirstInput(): void {
    setTimeout(() => this.firstInput.nativeElement.focus(), 200);
  }

  closeSidenav(): void {
    this.closeSidenavEvent.emit();
  }

  saveField(): void {
    const name = this.form.getRawValue().name;

    if (!this.isEditMode()) {
      const fieldExists: boolean = !!this.fields.find(
        existingField => existingField.name.toUpperCase() === name.toUpperCase()
      );

      if (fieldExists) {
        this.nameControl.setErrors({
          alreadyExists:
            'A field with name ' + name + ' already exists'
        });
        this.nameControl.markAsTouched();

        return;
      }
    }

    this.store.dispatch(new fieldFormActions.Reset());

    this.form.disable();

    this.submitFieldEvent.emit({
      ...this.form.value,
      name: name
    } as Field);
  }

  edit(field: Field): void {
    this.resetForm();
    this.title = `Edit Field: ${field.name}`;

    this.idControl.setValue(field.id);
    this.nameControl.setValue(field.name);
    this.typeControl.setValue(field.type);
    this.nullableControl.setValue(field.nullable);
    this.maxLengthControl.setValue(field.maxLength);
    this.minLengthControl.setValue(field.minLength);
    this.regularExpressionControl.setValue(field.regularExpression);
    this.formatControl.setValue(field.format);
    this.precisionControl.setValue(field.precision);
    this.scaleControl.setValue(field.scale);
  }

  isEditMode(): boolean {
    return this.title && this.title.startsWith('Edit Field');
  }

  isFormInvalid(): boolean {
    return this.form.disabled || this.form.invalid;
  }

  // noinspection JSMethodCanBeStatic
  isDateField() {
    return ['date', 'timestamp'].includes(this.typeControl.value);
  }

  onMinMaxChange() {
    this.minLengthControl.updateValueAndValidity();
    this.maxLengthControl.updateValueAndValidity();
  }

  resetFormValuesAfterTypeSelection(): void {
    const currField: Field = this.form.value;
    const newField: Field = {
      id: currField.id,
      name: currField.name,
      type: currField.type,
      nullable: currField.nullable
    };

    switch (newField.type) {
      case 'string': {
        newField.maxLength = 50;
        newField.minLength = 0;
        newField.regularExpression = '';
        break;
      }
      case 'date':
        newField.format = 'dd/MM/yyyy';
        break;
      case 'timestamp':
        newField.format = 'dd/MM/yyyy HH:mm:ss';
        break;
      case 'decimal': {
        newField.scale = 0;
        newField.precision = 38;
      }
    }

    this.form.reset(newField);
  }
}
