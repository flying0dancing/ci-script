import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from "@angular/core";

@Component({
  selector: "app-save-button",
  templateUrl: "./save-button.component.html"
})
export class SaveButtonComponent implements OnInit, OnDestroy {
  @Input() displayText: string;
  @Input() tooltip: string;
  @Input() disabled: boolean;
  @Input() loading: boolean;
  @Input() done: boolean;
  @Output() save: EventEmitter<any> = new EventEmitter();

  ngOnInit(): void {}

  emitSaveEvent(): void {
    this.save.emit();
  }

  ngOnDestroy(): void {
    this.save.unsubscribe();
  }
}
