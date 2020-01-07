import { cellRendererParams } from "@/fcr/dashboard/_tests/test.fixtures";
import { productSchema } from "@/fcr/dashboard/products/_tests/product-schema.mock";
import { ProductRowItem } from "@/fcr/dashboard/products/product-schema.interface";
import { ViewRuleButtonRendererComponent } from "@/fcr/dashboard/products/view-rule-button-renderer.component";
import { GridModule } from "@/fcr/dashboard/shared/grid";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { noop } from "rxjs";

describe("ViewRuleButtonRendererComponent", () => {
  let viewRuleComponent: ViewRuleButtonRendererComponent;
  let fixture: ComponentFixture<ViewRuleButtonRendererComponent>;
  let router: Router;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [GridModule, RouterTestingModule],
      declarations: [ViewRuleButtonRendererComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewRuleButtonRendererComponent);
    viewRuleComponent = fixture.componentInstance;
    viewRuleComponent.agInit(cellRendererParams);
    fixture.detectChanges();

    router = TestBed.get(Router);
  });

  describe("openRule", () => {
    it("should navigate to rules by product id and schema id", () => {
      const data: ProductRowItem = {
        ...productSchema,
        productId: 654,
        itemId: 23456
      };
      viewRuleComponent.agInit({ ...cellRendererParams, data });

      spyOn(router, "navigate").and.callFake(noop);

      viewRuleComponent.openRule();

      expect(router.navigate).toHaveBeenCalledWith(["rules"], {
        queryParams: { productId: 654, schemaId: 23456 }
      });
    });
  });
});
