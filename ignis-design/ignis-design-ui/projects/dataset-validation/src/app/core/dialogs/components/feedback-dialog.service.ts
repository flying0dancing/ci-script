import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { EnvironmentService } from '../../../../environments/environment.service';

@Injectable({ providedIn: 'root' })
export class FeedbackDialogService {
  host = window.location.origin;
  feedbackUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.feedbackUrl = `${this.environmentService.env.api.root}/feedback`;
  }

  post(message: string) {
    return this.http.post(this.feedbackUrl, {
      title: `New feedback from ${this.host}`,
      text: message
    });
  }
}
