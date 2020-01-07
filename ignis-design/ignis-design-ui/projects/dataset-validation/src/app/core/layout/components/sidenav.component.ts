import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'dv-sidenav',
  templateUrl: './sidenav.component.html',
  styleUrls: ['./sidenav.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidenavComponent {
  @Input() sidenavOpened: boolean;
  @Input() disableClose = false;
  @Input() mode = 'side';
}
