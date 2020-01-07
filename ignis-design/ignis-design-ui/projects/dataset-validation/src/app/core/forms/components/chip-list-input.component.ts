import { ENTER, TAB } from '@angular/cdk/keycodes';
import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  ViewChild
} from '@angular/core';
import { FormArray, FormControl, ValidatorFn } from '@angular/forms';
import {
  MatAutocomplete,
  MatAutocompleteSelectedEvent
} from '@angular/material/autocomplete';
import { MatChipInputEvent, MatChipList } from '@angular/material/chips';
import { Observable, Subscription } from 'rxjs';
import { map, startWith } from 'rxjs/operators';

@Component({
  selector: 'dv-chip-list-input',
  templateUrl: './chip-list-input.component.html'
})
export class ChipListInputComponent implements OnInit, OnChanges, OnDestroy {
  @Input() placeholder: string;
  @Input() class: string;
  @Input() chipInputFormControl: FormControl;
  @Input() chipsFormArray: FormArray;
  @Input() autoCompleteOptions: string[] = [];
  @Input() chipInputValidator: ValidatorFn;

  @Output()
  valueChanges: EventEmitter<string[]> = new EventEmitter();

  @ViewChild('auto', { static: true }) matAutocomplete: MatAutocomplete;
  @ViewChild('chipInput', { static: true }) chipInput: ElementRef<
    HTMLInputElement
  >;
  @ViewChild('chipList', { static: true }) chipList: MatChipList;

  separatorKeysCodes: number[] = [ENTER, TAB];
  filteredOptions: Observable<string[]>;

  private chipInputFormControlSubscription: Subscription;

  ngOnInit(): void {
    this.chipInputFormControl.setValidators(this.chipInputValidator);
    this.chipInputFormControlSubscription = this.subscribeToChipInputControl();

    this.filteredOptions = this.chipInputFormControl.valueChanges.pipe(
      startWith(<string>null),
      map((value: string) => {
        if (value) {
          return this.filterAutocompleteOptions(value);
        }

        if (this.autoCompleteOptions) {
          return this.autoCompleteOptions.slice();
        }
      })
    );
  }

  ngOnChanges(): void {
    this.chipInputFormControl.setValue(null);
    this.chipInputFormControl.setValidators(this.chipInputValidator);
  }

  ngOnDestroy(): void {
    this.chipInputFormControlSubscription.unsubscribe();
  }

  addChip(event: MatChipInputEvent): void {
    if (!this.matAutocomplete.isOpen && this.chipInputFormControl.valid) {
      const input = event.input;
      const value = event.value;

      if ((value || '').trim()) {
        this.chipsFormArray.push(new FormControl(value.trim()));
      }

      if (input) {
        input.value = '';
      }

      this.chipInputFormControl.setValue(null);
    }
  }

  selectChip(event: MatAutocompleteSelectedEvent): void {
    this.chipsFormArray.push(new FormControl(event.option.value));
    this.chipInput.nativeElement.value = '';
    this.chipInputFormControl.setValue(null);
    this.valueChanges.emit(this.chipsFormArray.value);
  }

  removeChip(index: number): void {
    if (index >= 0) {
      this.chipsFormArray.removeAt(index);
      this.valueChanges.emit(this.chipsFormArray.value);
    }
  }

  private subscribeToChipInputControl(): Subscription {
    return this.chipInputFormControl.statusChanges.subscribe(
      () => (this.chipList.errorState = this.chipInputFormControl.invalid)
    );
  }

  private filterAutocompleteOptions(value: string): string[] {
    return this.autoCompleteOptions.filter(
      option => option.toLowerCase().indexOf(value.toLowerCase()) === 0
    );
  }
}
