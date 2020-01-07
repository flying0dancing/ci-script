import { Component, Inject } from '@angular/core';
import { environment } from '@env/environment';

@Component({
  selector: 'app-http-auth-dialog',
  templateUrl: './http-auth-dialog.component.html'
})
export class HttpAuthDialogComponent {
  secondsText: string;

  constructor(@Inject('Window') private window: Window) {
    this.countdown(10);
  }

  login(): void {
    const redirect = encodeURIComponent(
      document.documentURI.replace(document.baseURI, '/'));

    this.window.location.href = `${environment.url.login}?redirect=${redirect}`;
  }

  private countdown(seconds: number) {
    this.secondsText = `${seconds} second${seconds === 1 ? '' : 's'}`;

    seconds === 0
      ? this.login()
      : setTimeout(() => this.countdown(seconds - 1), 1000);
  }
}
