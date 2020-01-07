import { Type } from "@angular/core";
import { features } from "@/core/api/features/_tests/features.mocks";
import { FeaturesService } from "@/core/api/features/features.service";
import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";

describe("FeaturesService", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: FeaturesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [FeaturesService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(FeaturesService);
  });

  it("should get features", () => {
    const expected = features;

    let actual;

    service.get().subscribe(resp => (actual = resp));

    const url = `${FeaturesService.generateGetUrl()}`;
    const req = httpMock.expectOne(url);

    req.flush(features);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });
});
