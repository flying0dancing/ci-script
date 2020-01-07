import { Component, ChangeDetectionStrategy, Input } from '@angular/core';

@Component({
  selector: 'dv-input-label',
  templateUrl: './input-label.component.html',
  styleUrls: ['./input-label.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class InputLabelComponent {
  @Input() value: string;

  @Input() showRequiredMarker?: boolean;

  @Input() error?: boolean;
}
