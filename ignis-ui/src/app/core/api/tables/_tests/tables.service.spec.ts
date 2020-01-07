import { Type } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { TablesService } from "../tables.service";

import { tables, uploadResponseSuccess } from "./tables.mocks";

describe("TablesService", () => {
  const tableId = 1;

  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: TablesService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [TablesService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(TablesService);
  });

  it("should get all tables", () => {
    const expected = tables;

    let actual;

    service.getTables().subscribe(resp => (actual = resp));

    const url = `${TablesService.generateUrl()}?sort=displayName,asc`;
    const req = httpMock.expectOne(url);

    req.flush(tables);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should delete a specified table", () => {
    let actual;

    service.delete(tableId).subscribe(resp => (actual = resp));

    const url = `${TablesService.generateTableIdUrl(tableId)}`;
    const req = httpMock.expectOne(url);

    req.flush({});

    httpMock.verify();

    expect(actual).toEqual({});
  });

  it("should post a new table", () => {
    const expectedBody = uploadResponseSuccess;
    const formData = new FormData();
    let actual;

    service.upload({ formData }).subscribe(response => (actual = response));

    const req = httpMock.expectOne(TablesService.generateFileUploadUrl());

    req.flush(uploadResponseSuccess);

    httpMock.verify();

    expect(actual.body).toEqual(expectedBody);
  });
});
