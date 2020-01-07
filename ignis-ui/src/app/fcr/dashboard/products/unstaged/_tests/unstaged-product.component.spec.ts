import { CoreModule } from "@/core";
import * as ProductsActions from "@/core/api/products/products.actions";
import { ProductsService } from "@/core/api/products/products.service";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { NAMESPACE } from "@/fcr/dashboard/products/products.selectors";
import { UnstagedProductsComponent } from "@/fcr/dashboard/products/unstaged/unstaged-products.component";
import { ConfirmDialogService } from "@/shared/dialogs";
import { ConfirmDialogComponent } from "@/shared/dialogs/confirm-dialog/confirm-dialog.component";
import * as DialogHelpers from "@/test-helpers/dialog.helpers";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { MatDialogRef } from "@angular/material/dialog";
import { RouterTestingModule } from "@angular/router/testing";
import { Store, StoreModule } from "@ngrx/store";
import { of } from "rxjs";

describe("UnstagedProductsComponent", () => {
  let component: UnstagedProductsComponent;
  let fixture: ComponentFixture<UnstagedProductsComponent>;
  let store: Store<any>;
  let confirmDialogService: ConfirmDialogService;
  const reducerMapKey = NAMESPACE;

  const mockDialogRef = {
    afterClosed: jasmine.createSpy("afterClosed").and.returnValue(of({}))
  };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule,
        CoreModule,
        StoreModule.forRoot(reducers),
        DialogHelpers.createDialogTestingModule({
          declarations: [ConfirmDialogComponent],
          entryComponents: [ConfirmDialogComponent],
          schemas: [NO_ERRORS_SCHEMA]
        })
      ],
      providers: [
        ProductsService,
        ConfirmDialogService,
        {
          provide: MatDialogRef,
          useValue: mockDialogRef
        }
      ],
      declarations: [UnstagedProductsComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    store = TestBed.get(Store);
    fixture = TestBed.createComponent(UnstagedProductsComponent);
    component = fixture.componentInstance;
    component.products = [];
    confirmDialogService = TestBed.get(ConfirmDialogService);
    spyOn(confirmDialogService, "confirm").and.returnValue(mockDialogRef);
    spyOn(store, "dispatch").and.callThrough();
    fixture.detectChanges();
  }));

  describe("delete", () => {
    it("should open the confirm product config delete dialog", () => {
      component.delete(100);

      expect(confirmDialogService.confirm).toHaveBeenCalledWith({
        title: "Are you sure?",
        content: "This product configuration will be deleted",
        confirmButtonText: "Yes",
        cancelButtonText: "No"
      });
    });

    it("should dispatch the delete action with id if afterClosed observable has defined value", () => {
      mockDialogRef.afterClosed.and.returnValue(of({}));

      const action = new ProductsActions.Delete({ reducerMapKey, id: 100 });

      component.delete(100);

      expect(store.dispatch).toHaveBeenCalledWith(action);
    });

    it("should not dispatch the delete action with id if afterClosed observable has undefined value", () => {
      mockDialogRef.afterClosed.and.returnValue(of(undefined));

      const action = new ProductsActions.Delete({ reducerMapKey, id: 100 });

      component.delete(100);

      expect(store.dispatch).not.toHaveBeenCalledWith(action);
    });
  });
});
