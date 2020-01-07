import { Component, EventEmitter, Input, OnDestroy, Output } from '@angular/core';

@Component({
  selector: 'dv-export-fab-button',
  templateUrl: './export-item-button.component.html'
})
export class ExportItemButtonComponent implements OnDestroy {
  @Input() title: string;
  @Input() mini = false;
  @Input() disabled = false;
  @Input() tooltipPosition = 'left';
  @Output() export: EventEmitter<any> = new EventEmitter();

  emitAddEvent(): void {
    this.export.emit();
  }

  ngOnDestroy(): void {
    this.export.unsubscribe();
  }
}
