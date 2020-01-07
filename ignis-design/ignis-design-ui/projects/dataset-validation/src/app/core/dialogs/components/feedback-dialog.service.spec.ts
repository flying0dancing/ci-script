import { HttpClient } from '@angular/common/http';
import {
  HttpClientTestingModule,
  HttpTestingController
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EnvironmentService } from '../../../../environments/environment.service';
import { FeedbackDialogService } from './feedback-dialog.service';
import { Type } from '@angular/core';

const envServiceStub = {
  env: {
    api: {
      root: 'myHost'
    }
  }
};

describe('FeedbackDialogService', () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: FeedbackDialogService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        FeedbackDialogService,
        { provide: EnvironmentService, useValue: envServiceStub }
      ]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(FeedbackDialogService);
  });

  it('should call post feedback url', () => {
    let actual;

    service.post('the message').subscribe(resp => (actual = resp));

    const testRequest = httpMock.expectOne('myHost/feedback');

    testRequest.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });

  it('should send feedback request', () => {
    let actual;

    service.host = 'http://myHost:8080';
    service.post('the message').subscribe(resp => (actual = resp));

    const testRequest = httpMock.expectOne('myHost/feedback');

    expect(testRequest.request.body).toEqual({
      title: 'New feedback from http://myHost:8080',
      text: 'the message'
    });
  });
});
