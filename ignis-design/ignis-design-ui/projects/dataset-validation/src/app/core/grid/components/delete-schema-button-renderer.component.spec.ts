import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { Store, StoreModule } from '@ngrx/store';
import * as ProductActions from '../../../product-configs/actions/product-configs.actions';
import { DeleteSchemaButtonRendererComponent } from './delete-schema-button-renderer.component';

describe('DeleteSchemaButtonRendererComponent', () => {
  let store: Store<any>;
  let fixture: ComponentFixture<DeleteSchemaButtonRendererComponent>;
  let component: DeleteSchemaButtonRendererComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.forRoot({}), MatDialogModule],
      declarations: [DeleteSchemaButtonRendererComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    store = TestBed.get(Store);
    fixture = TestBed.createComponent(DeleteSchemaButtonRendererComponent);
    component = fixture.componentInstance;

    spyOn(store, 'dispatch').and.callThrough();

    fixture.detectChanges();
  });

  describe('ngOnInit', () => {
    it('should dispatch a get products action', () => {
      component.ngOnInit();
      expect(store.dispatch).toHaveBeenCalledWith(new ProductActions.GetAll());
    });
  });
});
