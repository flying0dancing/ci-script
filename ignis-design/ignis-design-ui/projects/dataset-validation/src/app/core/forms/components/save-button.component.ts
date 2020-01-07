import {
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output
} from '@angular/core';
import { ShortcutsService } from '../../shortcuts/shortcuts.service';

@Component({
  selector: 'dv-save-button',
  templateUrl: './save-button.component.html'
})
export class SaveButtonComponent implements OnInit, OnDestroy {
  @Input() displayText: string;
  @Input() tooltip: string;
  @Input() disabled: boolean;
  @Input() loading: boolean;
  @Input() done: boolean;
  @Output() save: EventEmitter<any> = new EventEmitter();

  saveShortcut: string;

  constructor(private shortcutsService: ShortcutsService) {}

  ngOnInit(): void {
    const saveHotkey = this.shortcutsService.registerSaveHotkey(() =>
      this.emitSaveEvent()
    );
    this.saveShortcut = saveHotkey.formatted[0].toUpperCase();
  }

  emitSaveEvent(): void {
    this.save.emit();
  }

  ngOnDestroy(): void {
    this.save.unsubscribe();
  }
}
