import { ArrayValidators } from '@/fcr/dashboard/shared/validators';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { map, tap } from 'rxjs/operators';


@Component({
  selector: 'app-auto-complete',
  templateUrl: './auto-complete-form.component.html',
  styleUrls: ['./auto-complete-form.component.scss']
})
export class AutoCompleteFormComponent<T> implements OnChanges, OnInit {

  @Input()
  title: string;

  @Input()
  placeholder: string;

  @Input()
  formInputName: string;

  @Input()
  options: T[];
  @Output()
  valueChanged: EventEmitter<T> = new EventEmitter();
  keyedOptions: {[key: string]: T};
  form: FormGroup = this.fb.group({
    input: [null],
    autoComplete: [null]
  });

  constructor(
    private fb: FormBuilder) {}

  @Input()
  valueExtractor: (value: T) => string = x => x.toString();

  ngOnInit(): void {
    this.registerAutoComplete();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.options) {
      const newOptions: any[] = changes.options.currentValue;

      this.keyedOptions = {};
      newOptions.forEach(val => this.keyedOptions[this.valueExtractor(val)] = val);

      this.form.controls.autoComplete.setValue(newOptions);
      this.form.controls.input.setValidators([ArrayValidators.matchValue(newOptions.map(this.valueExtractor))]);
    }
  }

  toOptions(value: T[]) {
    return value.map(this.valueExtractor);
  }

  private registerAutoComplete() {
    this.form.controls.input.valueChanges
      .pipe(
        tap(value => this.emitIfSelected(value)),
        map((value) => {
            const availableOptions = this.options;

            return availableOptions.filter(
              option => this.valueExtractor(option).toLowerCase()
                .indexOf(value.toLowerCase()) !== -1);
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

}
