import {
  Component,
  Input,
  SimpleChanges,
  OnChanges,
  OnDestroy
} from '@angular/core';
import { FormBuilder, Validators, FormControl } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { Store, select } from '@ngrx/store';
import { Subject, combineLatest, BehaviorSubject } from 'rxjs';
import { takeUntil, filter, switchMap, map } from 'rxjs/operators';
import { ISO_DATE_FORMATS } from '../../core/utilities';
import { Update } from '../actions/pipeline-step-test.actions';
import { Create } from '../actions/pipeline-step-tests.actions';
import { PipelineStepTest } from '../interfaces/pipeline-step-test.interface';
import * as selectors from '../reducers';
import { Pipeline } from '../../pipelines/interfaces/pipeline.interface';
import { PipelineStep } from '../../pipelines/interfaces/pipeline-step.interface';
import { CommonValidators } from '../../core/forms/common.validators';
import * as moment from 'moment';

// Error when invalid control is invalid and dirty / touched
export class MyErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: FormControl | null): boolean {
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}

@Component({
  selector: 'dv-pipeline-step-test-form',
  templateUrl: './pipeline-step-test-form.component.html'
})
export class PipelineStepTestFormComponent implements OnChanges, OnDestroy {
  @Input() isEdit = false;

  @Input() pipeline: Pipeline;

  @Input() pipelineStepTest?: PipelineStepTest;

  @Input() pipelineStepTestLoading = false;

  stepOptions: PipelineStep[];

  hasCreated$ = this.store.pipe(
    select(selectors.GET_PIPELINE_STEP_TESTS_CREATED),
    filter(hasCreated => hasCreated)
  );

  formGroup = this.fb.group({
    name: ['', [CommonValidators.requiredTrimmed, Validators.maxLength(50)]],
    description: ['', Validators.maxLength(50)],
    testReferenceDate: null,
    pipelineStepId: ['', Validators.required]
  });

  nameControl = this.formGroup.get('name');

  descriptionControl = this.formGroup.get('description');

  testReferenceDateControl = this.formGroup.get('testReferenceDate');

  pipelineStepIdControl = this.formGroup.get('pipelineStepId');

  errorStateMatcher = new MyErrorStateMatcher();

  private pipelineStepTestSubject = new BehaviorSubject<PipelineStepTest>(null);

  private pipelineStepTest$ = this.pipelineStepTestSubject.asObservable();

  private pipelineStepTestLoadingSubject = new BehaviorSubject<boolean>(false);

  private pipelineStepTestLoading$ = this.pipelineStepTestLoadingSubject.asObservable();

  private isCreating$ = this.store.pipe(
    select(selectors.GET_PIPELINE_STEP_TESTS_CREATING)
  );

  private isUpdating$ = this.pipelineStepTest$.pipe(
    switchMap(pipelineStepTest =>
      this.store.pipe(
        select(selectors.getPipelineStepTestUpdating(pipelineStepTest && pipelineStepTest.id))))
  );

  isSubmitting$ = combineLatest([this.isCreating$, this.isUpdating$]).pipe(
    map(([isCreating, isUpdating]) => isCreating || isUpdating)
  );

  isFormDisabled$ = combineLatest([
    this.isSubmitting$,
    this.pipelineStepTestLoading$
  ]).pipe(map(([isSubmitting, isLoading]) => isSubmitting || isLoading));

  private unsubscribe$ = new Subject();

  constructor(private fb: FormBuilder, private store: Store<any>) {
    this.isFormDisabled$
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe(isDisabled => this.updateControlsStatus(isDisabled));

    this.hasCreated$
      .pipe(takeUntil(this.unsubscribe$))
      .subscribe(_ => this.formGroup.reset());
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.pipeline) {
      const pipeline = changes.pipeline.currentValue as Pipeline;

      this.stepOptions = pipeline ? pipeline.steps : [];
    }

    if (changes.pipelineStepTest) {
      const pipelineStepTest = changes.pipelineStepTest
        .currentValue as PipelineStepTest;

      this.pipelineStepTestSubject.next(pipelineStepTest);

      if (pipelineStepTest) {
        const {
          name,
          description,
          testReferenceDate,
          pipelineStepId
        } = pipelineStepTest;

        this.formGroup.setValue({
          name,
          description,
          testReferenceDate: moment(testReferenceDate),
          pipelineStepId
        });
      }
    }

    if (changes.pipelineStepTestLoading) {
      const pipelineStepTestLoading = changes.pipelineStepTestLoading
        .currentValue as boolean;

      this.pipelineStepTestLoadingSubject.next(pipelineStepTestLoading);
    }
  }

  ngOnDestroy() {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

  onSubmit() {
    const dateString = this.testReferenceDateControl.value.format('YYYY-MM-DD');

    const formValue = {
      ...this.formGroup.value,
      testReferenceDate: dateString
    };

    if (this.formGroup.valid) {
      if (this.isEdit) {
        this.store.dispatch(new Update(formValue, this.pipelineStepTest.id));
      } else {
        this.store.dispatch(new Create(formValue, this.pipeline.id));
      }
    }
  }

  private updateControlsStatus(isDisabled: boolean) {
    if (isDisabled) {
      this.nameControl.disable();
      this.descriptionControl.disable();
      this.testReferenceDateControl.disable();
      this.pipelineStepIdControl.disable();
    } else {
      this.nameControl.enable();
      this.descriptionControl.enable();
      this.testReferenceDateControl.enable();

      this.isEdit
        ? this.pipelineStepIdControl.disable()
        : this.pipelineStepIdControl.enable();
    }
  }
}
