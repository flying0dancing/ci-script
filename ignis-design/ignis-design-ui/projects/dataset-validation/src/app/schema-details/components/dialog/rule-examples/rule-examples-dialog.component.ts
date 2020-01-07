import {
  Component,
  EventEmitter,
  Inject,
  OnDestroy,
  OnInit
} from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { select, Store } from '@ngrx/store';
import { combineLatest, noop, Observable, Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { ResponseState } from '../../../../core/reducers/reducers-utility.service';
import { ShortcutsService } from '../../../../core/shortcuts/shortcuts.service';
import { Field, RuleResponse, Schema } from '../../../../schemas';
import { getSchema } from '../../../../schemas/reducers/schemas.selectors';
import * as ruleFormActions from '../../../actions/rule-form.actions';
import { RuleExample } from '../../../interfaces/rule-example.interface';
import * as ruleFormSelectors from '../../../selectors/rule-form.selector';
import { RulesRepository } from '../../../service/rules.repository';
import ICodeEditor = monaco.editor.ICodeEditor;

@Component({
  selector: 'dv-examples-dialog',
  templateUrl: 'rule-examples-dialog.component.html',
  styleUrls: ['rule-examples-dialog.component.scss']
})
export class RuleExamplesDialogComponent implements OnInit, OnDestroy {
  contextFields$: Observable<Field[]>;
  ruleExamples: RuleExample[] = [];
  rule: RuleResponse;
  initialExpression: string;

  errors$ = this.store
    .pipe(select(ruleFormSelectors.ERRORS))
    .pipe(
      map(errors => errors.filter(error => error.errorCode !== 'expression'))
    );

  addEmptyExampleEvent: EventEmitter<any> = new EventEmitter();

  lineNumber: number;
  columnNumber: number;

  saveExampleShortcut: string;

  examplesLoaded = true;
  ruleLoading$ = this.store.pipe(select(ruleFormSelectors.LOADING));

  private loaded$ = this.store.pipe(select(ruleFormSelectors.LOADED));
  private loadedSuccessfully$ = combineLatest([
    this.errors$,
    this.loaded$
  ]).pipe(filter(([errors, loaded]) => errors.length < 1 && loaded));

  private loadedSuccessfullySubscription: Subscription;
  private updateDialogSizeSubscription: Subscription;
  private getExamplesSubscription: Subscription;

  private schemaId: number;
  private productId: number;
  private changeCursorPositionDisposable: monaco.IDisposable;

  constructor(
    private dialogRef: MatDialogRef<RuleExamplesDialogComponent>,
    @Inject(MAT_DIALOG_DATA) private data,
    private rulesRepository: RulesRepository,
    private store: Store<ResponseState>,
    private shortcutsService: ShortcutsService
  ) {}

  initEditor(editor: ICodeEditor): void {
    this.changeCursorPositionDisposable = editor.onDidChangeCursorPosition(
      cursorChangedEvent => {
        this.lineNumber = cursorChangedEvent.position.lineNumber;
        this.columnNumber = cursorChangedEvent.position.column;
      }
    );
  }

  ngOnInit(): void {
    this.rule = this.data.rule;
    this.schemaId = this.data.schemaId;
    this.productId = this.data.productId;

    this.updateInitialExpression();

    this.loadedSuccessfullySubscription = this.loadedSuccessfully$.subscribe(
      () => this.updateInitialExpression()
    );

    const rule$ = this.store.pipe(select(getSchema(this.schemaId))).pipe(
      map((schema: Schema) =>
        schema.validationRules.find(rule => rule.id === this.rule.id)
      ),
      filter(rule => !!rule)
    );

    this.getExamplesSubscription = rule$.subscribe(() =>
      this.getTestedExamples()
    );

    this.contextFields$ = rule$.pipe(map(rule => rule.contextFields));
    this.updateDialogSizeSubscription = this.contextFields$.subscribe(
      contextFields => {
        const dialogWidth = `${(contextFields.length + 2) * 220}px`;

        if (this.ruleExamples.length > 8) {
          this.dialogRef.updateSize(dialogWidth, `940px`);
        } else {
          this.dialogRef.updateSize(dialogWidth);
        }
      }
    );
    this.setupShortcuts();
  }

  private setupShortcuts(): void {
    this.shortcutsService.registerCloseHotkey(noop);

    const saveExampleHotkey = this.shortcutsService.registerSaveHotkey(noop);
    this.saveExampleShortcut = saveExampleHotkey.formatted[0].toUpperCase();
  }

  private getTestedExamples(): void {
    this.examplesLoaded = false;

    this.rulesRepository
      .getExamples(this.productId, this.schemaId, this.rule.id)
      .subscribe((examples: RuleExample[]) => {
        this.ruleExamples = examples;

        this.examplesLoaded = true;
      });
  }

  private updateInitialExpression(): void {
    this.initialExpression = this.rule.expression;
  }

  ngOnDestroy(): void {
    this.loadedSuccessfullySubscription.unsubscribe();
    this.updateDialogSizeSubscription.unsubscribe();
    this.getExamplesSubscription.unsubscribe();

    this.changeCursorPositionDisposable.dispose();
  }

  undoExpressionChanges(): void {
    this.rule.expression = this.initialExpression;

    this.store.dispatch(new ruleFormActions.Reset());
  }

  isInitialExpression(): boolean {
    return this.initialExpression.trim() === this.rule.expression.trim();
  }

  addEmptyExample(): void {
    const lastExample = this.ruleExamples[this.ruleExamples.length - 1];

    if (this.ruleExamples.length < 1 || lastExample.expectedResult) {
      this.ruleExamples = [
        ...this.ruleExamples,
        {
          id: null,
          expectedResult: null,
          actualResult: null,
          exampleFields: {},
          unexpectedError: null
        }
      ];

      this.addEmptyExampleEvent.emit();
    }
  }

  removeExample(nodeId: number): void {
    const selectedExamples = this.ruleExamples.filter(
      (_, index) => index !== nodeId
    );

    this.ruleExamples = [...selectedExamples];
  }

  isExampleFieldInvalid(): boolean {
    return this.ruleExamples.some(
      (example: RuleExample) => example.expectedResult === null
    );
  }

  saveExamples(): void {
    this.store.dispatch(
      new ruleFormActions.Post(this.productId, this.schemaId, {
        id: this.rule.id,
        name: this.rule.name,
        version: this.rule.version,
        ruleId: this.rule.ruleId,
        validationRuleType: this.rule.validationRuleType,
        validationRuleSeverity: this.rule.validationRuleSeverity,
        description: this.rule.description,
        startDate: this.rule.startDate,
        endDate: this.rule.endDate,
        expression: this.rule.expression,
        contextFields: this.rule.contextFields,
        ruleExamples: this.ruleExamples
      })
    );
  }

  close(): void {
    this.dialogRef.close(this.rule.expression);

    this.dialogRef.afterClosed().subscribe(() => (this.dialogRef = undefined));
  }
}
