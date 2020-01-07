import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { Hotkey } from 'angular2-hotkeys';
import { ShortcutsService } from '../../shortcuts/shortcuts.service';

@Component({
  selector: 'dv-add-fab-button',
  templateUrl: './add-item-button.component.html'
})
export class AddItemButtonComponent implements OnInit, OnDestroy {
  @Input() title: string;
  @Input() mini = false;
  @Input() disabled = false;
  @Input() tooltipPosition = 'left';
  @Input() shortcutKey = 'alt+n';
  @Output() add: EventEmitter<any> = new EventEmitter();

  newShortcut: string;

  constructor(private shortcutsService: ShortcutsService) {}

  ngOnInit(): void {
    const newHotkey: Hotkey = this.shortcutsService.registerAddItemHotkey(
      this.shortcutKey,
      () => this.emitAddEvent()
    );
    this.newShortcut = newHotkey.formatted[0].toUpperCase();
  }

  emitAddEvent(): void {
    this.add.emit(this.shortcutKey);
  }

  ngOnDestroy(): void {
    this.add.unsubscribe();
  }
}
