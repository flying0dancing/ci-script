import {
  AfterViewChecked,
  ChangeDetectorRef,
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DATE_FORMATS } from '@angular/material/core';
import { Moment } from 'moment';
import { ISO_DATE_FORMATS } from '../../utilities';

@Component({
  selector: 'dv-inline-date-edit',
  templateUrl: './inline-date-edit-input.component.html',
  styleUrls: ['./inline-edit-input.component.scss'],
  providers: [{ provide: MAT_DATE_FORMATS, useValue: ISO_DATE_FORMATS }]
})
export class InlineDateEditInputComponent implements OnInit, AfterViewChecked {
  @ViewChild('editInput', { static: false }) editValRef: ElementRef;

  @Input() label;
  @Input() required = false;
  @Input() disabled = false;
  @Input() loading = false;
  @Input() initialValue = '';
  @Input() width = '200px';

  @Input() editFunction: (val: Moment) => void;

  inputForm: FormGroup;
  editing = false;
  hover = false;

  constructor(
    private changeDetector: ChangeDetectorRef,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    const validator = this.required ? Validators.required : null;
    this.inputForm = this.fb.group({
      val: [null, validator]
    });
  }

  editConfirm() {
    const valueControl = this.inputForm.controls['val'];
    if (!this.inputForm.valid || !this.editing) {
      return;
    }

    this.editing = false;
    this.hover = false;

    if (valueControl.dirty) {
      this.editFunction(valueControl.value);
    }
  }

  onKey(event) {
    if (event.key === 'Enter') {
      this.editConfirm();
    }
    if (event.key === 'Escape') {
      this.editing = false;
      this.hover = false;
    }
  }

  editValue() {
    if (this.disabled) {
      return;
    }

    this.inputForm.controls['val'].setValue(this.initialValue);
    this.editing = true;
  }

  ngAfterViewChecked(): void {
    if (this.editing) {
      this.editValRef.nativeElement.focus();
      this.changeDetector.detectChanges();
    }
  }
}
