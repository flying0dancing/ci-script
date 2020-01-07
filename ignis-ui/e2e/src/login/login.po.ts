import { browser, by, element } from 'protractor';

export class LoginPage {
  navigateTo() {
    return browser.get('/login');
  }

  getUsernameInput() {
    return element(by.id('mat-input-0'));
  }

  getPasswordInput() {
    return element(by.id('mat-input-1'));
  }

  getSubmitButton() {
    return element(by.css('button'));
  }

  getErrorMessageText() {
    return element(by.id('mat-error-0')).getText();
  }
}
