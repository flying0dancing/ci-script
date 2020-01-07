import { Injectable } from '@angular/core';
import { Hotkey, HotkeysService } from 'angular2-hotkeys';

export const CHEAT_SHEET_SHORTCUT = 'alt+/';
const saveShortcut = 'alt+s';
const closeShortcut = 'esc';

const allowInInputs = ['INPUT', 'SELECT', 'TEXTAREA'];

@Injectable({ providedIn: 'root' })
export class ShortcutsService {
  private static createSaveHotkey(): Hotkey {
    return new Hotkey(saveShortcut, () => false, allowInInputs, 'Save changes');
  }

  private static createAddItemHotkey(shortcut: string): Hotkey {
    return new Hotkey(shortcut, () => false, allowInInputs, 'Create new');
  }

  private static createCloseHotkey(): Hotkey {
    return new Hotkey(closeShortcut, () => false, allowInInputs, 'Close panel');
  }

  constructor(public hotkeysService: HotkeysService) {
    const cheatSheetHotkey = this.hotkeysService.hotkeys.find(
      hotkey => hotkey.combo.toString() === CHEAT_SHEET_SHORTCUT
    );
    cheatSheetHotkey.allowIn = allowInInputs;
  }

  keepCheatSheetShortcutsOnly(): void {
    this.hotkeysService.hotkeys = this.hotkeysService.hotkeys.filter(
      hotkey => hotkey.combo.toString() === CHEAT_SHEET_SHORTCUT
    );
  }

  toggleCheatSheet(): void {
    this.hotkeysService.cheatSheetToggle.next();
  }

  findCheatSheetShortcut(): Hotkey {
    return this.hotkeysService.hotkeys.find(
      hotkey => hotkey.combo.toString() === CHEAT_SHEET_SHORTCUT
    );
  }

  registerSaveHotkey(saveCallback: () => void): Hotkey {
    return this.registerHotkey(
      ShortcutsService.createSaveHotkey(),
      saveCallback
    );
  }

  registerAddItemHotkey(shortcut: string, addItemCallback: () => void): Hotkey {
    return this.registerHotkey(
      ShortcutsService.createAddItemHotkey(shortcut),
      addItemCallback
    );
  }

  registerCloseHotkey(closeCallback: () => void): Hotkey {
    return this.registerHotkey(
      ShortcutsService.createCloseHotkey(),
      closeCallback
    );
  }

  private registerHotkey(defaultHotkey: Hotkey, callback: () => void): Hotkey {
    const hotkey = defaultHotkey;
    hotkey.callback = () => {
      callback();
      return false;
    };
    const newHotkey = this.hotkeysService.add(hotkey) as Hotkey;
    this.deDuplicateHotkeys();
    return newHotkey;
  }

  private deDuplicateHotkeys(): void {
    const hotkeyByCombo = this.hotkeysService.hotkeys.reduce(
      (hotkeysByCombo, current) => {
        hotkeysByCombo[current.combo[0].toLowerCase()] = current;

        return hotkeysByCombo;
      },
      {}
    );
    this.hotkeysService.hotkeys = Object.values(hotkeyByCombo);
  }
}
