import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { map, tap } from 'rxjs/operators';
import { CommonValidators } from '../common.validators';


@Component({
  selector: 'dv-auto-complete',
  templateUrl: './auto-complete-form.component.html',
  styleUrls: ['./auto-complete-form.component.scss'],
})
export class AutoCompleteFormComponent<T> implements OnChanges, OnInit {

  @Input()
  options: T[] = [];

  @Input()
  initialValue: T;

  @Input()
  valueExtractor: (t: T) => string = x => x.toString();

  @Input()
  title: string;

  @Input()
  placeholder: string;

  @Input()
  formInputName: string;

  @Input()
  disabled: boolean;

  @Output()
  valueChanged: EventEmitter<T> = new EventEmitter();

  form = this.fb.group({
    input: [null],
    autoComplete: []
  });

  keyedOptions: { [key: string]: T };

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.registerAutoComplete();
    if (!!this.initialValue) {
      this.form.controls.input.setValue(this.valueExtractor(this.initialValue));
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.options && !!changes.options.currentValue) {
      const newOptions: any[] = changes.options.currentValue;

      this.keyedOptions = {};
      newOptions.forEach(val => this.keyedOptions[this.valueExtractor(val)] = val);

      this.form.controls.autoComplete.setValue(newOptions);
      this.form.controls.input.setValidators([CommonValidators.matchValue(newOptions.map(this.valueExtractor))]);

    }

    if (changes.initialValue && !!changes.initialValue.currentValue) {
      this.form.controls.input.setValue(this.valueExtractor(changes.initialValue.currentValue));
    }

    if (changes.disabled && changes.disabled.currentValue) {
      this.form.disable();
    }

    if (changes.disabled && !changes.disabled.currentValue) {
      this.form.enable();
    }
  }

  private registerAutoComplete() {
    this.form.controls.input.valueChanges
      .pipe(
        tap(value => this.emitIfSelected(value)),
        map((value) => {
            const availableOptions = !!this.options ? this.options : [];

            const matchRegex = new RegExp(CommonValidators.escapeRegex(value), 'i');

            return availableOptions.filter(
              option => this.valueExtractor(option).match(matchRegex));
          }
        )
      ).subscribe(filteredOptions =>
      this.form.controls.autoComplete.setValue(filteredOptions)
    );
  }

  private emitIfSelected(value) {
    const foundValue = this.keyedOptions[value];
    if (!!foundValue) {
      this.valueChanged.emit(foundValue);
    }
  }

  toOptions(value: T[]) {
    return !!value ? value.map(this.valueExtractor) : [];
  }

}
