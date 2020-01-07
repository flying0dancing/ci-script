import { browser } from 'protractor';

import { LoginPage } from './login.po';

describe('Login', () => {
  let page: LoginPage;

  beforeEach(() => {
    page = new LoginPage();
    page.navigateTo();
  });

  it('should redirect to the dashboard page on login success', () => {
    page.getUsernameInput().sendKeys('admin');
    page.getPasswordInput().sendKeys('password');
    page.getSubmitButton().click();

    browser.waitForAngular();

    expect(browser.getCurrentUrl()).toEqual(`${browser.baseUrl}/dashboard`);

  });

  it('should display an error message on login fail', () => {
    page.getUsernameInput().sendKeys('invalid');
    page.getPasswordInput().sendKeys('password');
    page.getSubmitButton().click();

    expect(page.getErrorMessageText()).toEqual('Bad credentials');
  });
});
