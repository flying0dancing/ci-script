import {
  ChangeDetectionStrategy,
  Component,
  HostBinding,
  Input
} from '@angular/core';

@Component({
  selector: 'dv-container',
  template: `
    <ng-content></ng-content>
  `,
  styleUrls: ['./container.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ContainerComponent {
  @Input()
  set size(value: string) {
    if (this.sizes.indexOf(value) !== -1) {
      this._sizeClass = value;
    }
  }

  private _sizeClass = 'medium';

  private sizes = ['small', 'medium', 'large'];

  @HostBinding('class')
  get sizeClass() {
    return this._sizeClass;
  }
}
