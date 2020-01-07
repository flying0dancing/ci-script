import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';

@Component({
  selector: 'dv-import-fab-button',
  templateUrl: './import-item-button.component.html'
})
export class ImportItemButtonComponent implements OnDestroy {
  @Input() title: string;
  @Input() mini = false;
  @Input() disabled = false;
  @Input() tooltipPosition = 'left';
  @Output() import: EventEmitter<any> = new EventEmitter();

  emitAddEvent(): void {
    this.import.emit();
  }

  ngOnDestroy(): void {
    this.import.unsubscribe();
  }
}
