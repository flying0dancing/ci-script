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

@Component({
  selector: 'dv-inline-edit',
  templateUrl: './inline-edit-input.component.html',
  styleUrls: ['./inline-edit-input.component.scss']
})
export class InlineEditComponent implements OnInit, AfterViewChecked {
  @ViewChild('editInput', { static: false }) editValRef: ElementRef;
  @ViewChild('uppercaseEdit', { static: false })
  upperCaseEditValRef: ElementRef;

  @Input() label;
  @Input() makeUppercase = false;
  @Input() type = 'text';
  @Input() required = false;
  @Input() disabled = false;
  @Input() loading = false;
  @Input() initialValue = '';
  @Input() width = '200px';

  @Input() editFunction: (val) => void;

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
      val: [this.initialValue, validator]
    });
  }

  onBlur() {
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
      this.onBlur();
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
      if (this.makeUppercase) {
        this.upperCaseEditValRef.nativeElement.focus();
      } else {
        this.editValRef.nativeElement.focus();
      }

      this.changeDetector.detectChanges();
    }
  }
}
