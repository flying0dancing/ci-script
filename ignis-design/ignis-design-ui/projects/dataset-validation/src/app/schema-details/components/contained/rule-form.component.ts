import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { AbstractControl, FormBuilder } from '@angular/forms';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { MatDialog } from '@angular/material/dialog';
import { Store } from '@ngrx/store';
import * as moment from 'moment';
import { EditorComponent } from 'ngx-monaco-editor/editor.component';
import { Subscription } from 'rxjs/internal/Subscription';
import { filter, map, withLatestFrom } from 'rxjs/operators';
import { EditorUtils } from '../../../core/forms/editor-utils';
import { EnumEntry, toEnumEntries } from '../../../core/forms/enumEntry';
import { FIELD_ERRORS } from '../../../core/forms/field.validators';
import { ResponseState } from '../../../core/reducers/reducers-utility.service';
import { ShortcutsService } from '../../../core/shortcuts/shortcuts.service';
import { ISO_DATE_FORMATS } from '../../../core/utilities';
import { ApiError } from '../../../core/utilities/interfaces/errors.interface';
import {
  Field,
  RuleRequest,
  RuleResponse,
  ValidationRuleExample,
  ValidationRuleSeverity,
  ValidationRuleType
} from '../../../schemas';
import * as ruleFormActions from '../../actions/rule-form.actions';
import { toRuleExamples } from '../../functions/schema-details.functions';
import * as ruleFormSelectors from '../../selectors/rule-form.selector';
import { RuleExamplesDialogComponent } from '../dialog/rule-examples/rule-examples-dialog.component';
import { RuleHelpDialogComponent } from '../dialog/rule-help-dialog.component';
import ICodeEditor = monaco.editor.ICodeEditor;

const VALIDATION_RULE_TYPE_ENTRIES = toEnumEntries(ValidationRuleType);
const VALIDATION_RULE_SEVERITY_ENTRIES = toEnumEntries(ValidationRuleSeverity);
const DATE_ORDER_ERROR_CODE = 'datesOrdered';
const EXPRESSION_ERROR_CODE = 'expression';
const SAVE_BUTTON_TEXT = 'Save';

@Component({
  selector: 'dv-rule-form',
  templateUrl: './rule-form.component.html',
  styleUrls: ['./rule-form.component.scss'],
  providers: [{ provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMATS }]
})
export class RuleFormComponent implements OnInit, OnDestroy {
  @Input() fields: Field[];
  @Input() schemaId: number;
  @Input() productId: number;

  @Output() submitRuleEvent: EventEmitter<RuleRequest> = new EventEmitter();
  @Output() closeSidenavEvent: EventEmitter<any> = new EventEmitter();

  @ViewChild('firstInput', { static: true }) firstInput: ElementRef;

  validationRuleTypes: EnumEntry[] = VALIDATION_RULE_TYPE_ENTRIES;
  validationRuleSeverities: EnumEntry[] = VALIDATION_RULE_SEVERITY_ENTRIES;

  loading$ = this.store.select(ruleFormSelectors.LOADING);
  loaded$ = this.store.select(ruleFormSelectors.LOADED);

  errors$ = this.store.select(ruleFormSelectors.ERRORS);

  saving$ = this.loading$.pipe(
    withLatestFrom(this.loaded$),
    map(([loading, loaded]) => loading && !loaded)
  );
  title: string;
  minVersion = 1;
  saveButtonText = SAVE_BUTTON_TEXT;
  closeRuleFormShortcut: string;

  form = this.formBuilder.group({
    id: null,
    ruleId: null,
    name: null,
    version: this.minVersion,
    validationRuleType: null,
    validationRuleSeverity: null,
    description: null,
    startDate: null,
    endDate: null
  });
  ruleIdControl = this.form.get('ruleId');
  nameControl = this.form.get('name');
  versionControl = this.form.get('version');
  validationRuleTypeControl = this.form.get('validationRuleType');
  validationRuleSeverityControl = this.form.get('validationRuleSeverity');
  descriptionControl = this.form.get('description');
  startDateControl = this.form.get('startDate');
  endDateControl = this.form.get('endDate');

  fieldErrorsProp = FIELD_ERRORS;
  globalErrors: ApiError[] = [];

  expression: string;
  expressionError: string;
  private validationRuleExamples: ValidationRuleExample[];

  private savedSubscription: Subscription;
  private savingSubscription: Subscription;
  private errorsSubscription: Subscription;
  private startDateChangedSubscription: Subscription;
  private endDateChangedSubscription: Subscription;
  private completionDisposable: monaco.IDisposable;

  private formFields: string[] = Object.keys(this.form.getRawValue());
  private editor: ICodeEditor;
  private helpDialogOpen: boolean;

  constructor(
    private formBuilder: FormBuilder,
    private store: Store<ResponseState>,
    public dialog: MatDialog,
    private shortcutsService: ShortcutsService
  ) {}

  onInit(editor: ICodeEditor & EditorComponent) {
    this.completionDisposable = (<any>(
      window
    )).monaco.languages.registerCompletionItemProvider(EditorUtils.jexlId, {
      provideCompletionItems: () =>
        this.fields.map(field => {
          return { ...EditorUtils.createFieldCompletionItemProvider(field) };
        })
    });
    this.editor = editor;
  }

  ngOnInit(): void {
    this.setupShortcuts();

    this.setupOnErrors();
    this.setupOnSaving();
    this.setupOnSaved();
  }

  private setupShortcuts() {
    const closeRuleFormHotkey = this.shortcutsService.registerCloseHotkey(
      () => {
        if (this.form.disabled) {
          this.closeSidenav();
        }
      }
    );
    this.closeRuleFormShortcut = closeRuleFormHotkey.formatted[0].toUpperCase();
    this.shortcutsService.registerSaveHotkey(() => {
      if (!this.form.disabled) {
        this.submit();
      }
    });
  }

  private setupOnErrors(): void {
    this.errorsSubscription = this.errors$
      .pipe(filter(errors => errors.length > 0))
      .subscribe(errors => {
        this.form.enable();
        this.enableEditor();

        this.saveButtonText = SAVE_BUTTON_TEXT;
        this.setExpressionError(errors);
        this.setFieldErrors(errors);
        this.setGlobalErrors(errors);
      });
  }

  private setFieldErrors(errors: ApiError[]): void {
    this.formFields.forEach(fieldName =>
      this.setErrorsOnField(
        this.findErrorsByField(errors, fieldName),
        this.form.get(fieldName)
      )
    );

    const datesOrderedError = this.findErrorsByField(
      errors,
      DATE_ORDER_ERROR_CODE
    );
    this.setErrorsOnField(datesOrderedError, this.startDateControl);
    this.setErrorsOnField(datesOrderedError, this.endDateControl);

    this.startDateChangedSubscription = this.startDateControl.valueChanges.subscribe(
      () => this.resetDateErrors()
    );
    this.endDateChangedSubscription = this.endDateControl.valueChanges.subscribe(
      () => this.resetDateErrors()
    );
  }

  private resetDateErrors(): void {
    this.startDateControl.setErrors(null);
    this.endDateControl.setErrors(null);
  }

  private findErrorsByField(errors: ApiError[], fieldName: string): string {
    return errors
      .filter(err => err.errorCode === fieldName)
      .map(err => err.errorMessage)
      .join(' | ');
  }

  private setErrorsOnField(errors: string, formControl: AbstractControl): void {
    if (errors) {
      const fieldErrors = {};
      fieldErrors[this.fieldErrorsProp] = errors;

      formControl.setErrors(fieldErrors);
      formControl.markAsTouched();
    }
  }

  private setGlobalErrors(errors: ApiError[]): void {
    this.globalErrors = errors.filter(
      err =>
        err.errorCode !== DATE_ORDER_ERROR_CODE &&
        err.errorCode !== EXPRESSION_ERROR_CODE &&
        !this.formFields.includes(err.errorCode)
    );
  }

  private setExpressionError(errors: ApiError[]): void {
    const expressionError = errors.find(
      err => err.errorCode === EXPRESSION_ERROR_CODE
    );
    if (expressionError) {
      this.expressionError = expressionError.errorMessage;
    }
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
      .pipe(
        withLatestFrom(this.errors$),
        filter(([saving, errors]) => !saving && errors.length === 0)
      )
      .subscribe(() => {
        if (this.isEditMode()) {
          this.title = `Edited Rule: ${this.form.getRawValue().ruleId}`;
        } else {
          this.title = `Created Rule: ${this.form.getRawValue().ruleId}`;
        }
        this.saveButtonText = 'Saved';
        this.expressionError = null;
      });
  }

  ngOnDestroy(): void {
    this.savingSubscription.unsubscribe();
    this.savedSubscription.unsubscribe();
    this.errorsSubscription.unsubscribe();

    this.completionDisposable.dispose();
    this.editor.dispose();
  }

  resetForm(): void {
    this.store.dispatch(new ruleFormActions.Reset());

    this.form.enable();
    this.form.reset({ version: this.minVersion });
    this.form.setErrors({});
    this.enableEditor();

    this.title = 'Create Rule';
    this.saveButtonText = SAVE_BUTTON_TEXT;

    this.expression = null;
    this.expressionError = null;

    this.globalErrors = [];

    this.focusOnFirstInput();
  }

  private focusOnFirstInput(): void {
    setTimeout(() => this.firstInput.nativeElement.focus(), 200);
  }

  closeSidenav(): void {
    this.closeSidenavEvent.emit();
  }

  submit(): void {
    this.expressionError = null;
    this.globalErrors = [];

    this.disableEditor();
    this.store.dispatch(new ruleFormActions.Reset());
    this.form.disable();

    this.submitRuleEvent.emit({
      ...this.formAsRule(),
      ruleExamples: toRuleExamples(this.validationRuleExamples)
    } as RuleRequest);
  }

  private formAsRule() {
    return {
      id: this.form.get('id').value,
      ruleId: this.ruleIdControl.value,
      name: this.nameControl.value,
      version: this.versionControl.value,
      validationRuleType: this.validationRuleTypeControl.value,
      validationRuleSeverity: this.validationRuleSeverityControl.value,
      description: this.descriptionControl.value,
      startDate: this.startDateControl.value.format(
        ISO_DATE_FORMATS.parse.dateInput
      ),
      endDate: this.endDateControl.value.format(
        ISO_DATE_FORMATS.parse.dateInput
      ),
      expression: this.expression
    };
  }

  minMaxDates(): void {
    this.startDateControl.setValue(moment('1970-01-01'));
    this.endDateControl.setValue(moment('9999-12-31'));
  }

  edit(rule: RuleResponse): void {
    this.enableEditor();
    this.resetForm();
    this.title = `Edit Rule: ${rule.ruleId}`;

    this.expression = rule.expression;
    this.validationRuleExamples = rule.validationRuleExamples;

    const startDate = moment(rule.startDate);
    const endDate = moment(rule.endDate);
    const {
      id,
      ruleId,
      name,
      version,
      validationRuleType,
      validationRuleSeverity,
      description
    } = rule;
    this.form.setValue({
      id,
      ruleId,
      name,
      version,
      validationRuleType,
      validationRuleSeverity,
      startDate,
      endDate,
      description
    });
  }

  private disableEditor() {
    this.editor.updateOptions({ readOnly: true });
  }

  private enableEditor() {
    this.editor.updateOptions({ readOnly: false });
  }

  isEditMode(): boolean {
    return this.title && this.title.startsWith('Edit Rule');
  }

  isFormInvalid(): boolean {
    return (
      this.form.disabled || this.form.invalid || this.expression.length < 1
    );
  }

  openHelpDialog(): void {
    if (this.helpDialogOpen) {
      return;
    }

    let dialogRef = this.dialog.open(RuleHelpDialogComponent, {
      width: '50vw',
      position: {
        left: '5vw'
      },
      hasBackdrop: false
    });

    dialogRef.afterClosed().subscribe(() => {
      this.helpDialogOpen = false;
      dialogRef = undefined;
    });

    this.helpDialogOpen = true;
  }

  openExamplesDialog(): void {
    let examplesDialogRef = this.dialog.open(RuleExamplesDialogComponent, {
      disableClose: true,
      minWidth: '860px',
      maxWidth: '1800px',
      data: {
        schemaId: this.schemaId,
        productId: this.productId,
        rule: {
          ...this.formAsRule()
        } as RuleResponse
      }
    });

    examplesDialogRef.afterClosed().subscribe(expression => {
      if (!this.isEditMode()) {
        this.expression = expression;
        this.disableEditor();
        this.setupShortcuts();
      }
      examplesDialogRef = undefined;
    });
  }
}
