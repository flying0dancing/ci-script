import { Type } from "@angular/core";
import { products } from "@/core/api/products/_tests/products.mocks";
import { ProductsService } from "@/core/api/products/products.service";
import { HttpClient } from "@angular/common/http";
import {
  HttpClientTestingModule,
  HttpTestingController
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";

describe("ProductsService", () => {
  let httpMock: HttpTestingController;
  let http: HttpClient;
  let service: ProductsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProductsService]
    });

    httpMock = TestBed.get(HttpTestingController as Type<
      HttpTestingController
    >);
    http = TestBed.get(HttpClient);
    service = TestBed.get(ProductsService);
  });

  it("should get products", () => {
    const expected = products;

    let actual;

    service.get().subscribe(resp => (actual = resp));

    const url = `${ProductsService.generateGetUrl()}`;
    const req = httpMock.expectOne(url);

    req.flush(products);

    httpMock.verify();

    expect(actual).toEqual(expected);
  });

  it("should delete products", () => {
    const id = 1234;

    const expected = id;

    let actual;

    service.delete(id).subscribe(resp => (actual = resp));

    const url = `${ProductsService.generateGetUrl()}` + "/" + id;
    const req = httpMock.expectOne(url);

    req.flush({ id });

    httpMock.verify();

    expect(actual).toEqual(expected);
  });
});
