import { Type } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { DatasetsService } from "../datasets.service";

import { datasets, sourceFiles } from "./datasets.mocks";

describe("DatasetsService", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: DatasetsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DatasetsService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(DatasetsService);
  });

  it("should get datasets", () => {
    const expected = datasets;

    let actual;

    service.get().subscribe(resp => (actual = resp));

    const url = `${DatasetsService.generateGetUrl()}?sort=name,asc`;
    const req = httpMock.expectOne(url);

    req.flush(datasets);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should get source files", () => {
    const expected = sourceFiles;

    let actual;

    service.getSourceFiles().subscribe(resp => (actual = resp));

    const url = DatasetsService.generateGetSourceFilesUrl();
    const req = httpMock.expectOne(url);

    req.flush(sourceFiles);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });
});
