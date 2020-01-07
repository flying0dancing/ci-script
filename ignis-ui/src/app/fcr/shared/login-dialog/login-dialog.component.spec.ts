import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { Store, StoreModule } from "@ngrx/store";
import { LoginDialogComponent } from "./login-dialog.component";

class MatDialogRefMock {}

describe("SchemaUploadDialogComponent", () => {
  let component: LoginDialogComponent;
  let fixture: ComponentFixture<LoginDialogComponent>;
  let store: Store<any>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule,
        StoreModule.forRoot(reducers)
      ],
      declarations: [LoginDialogComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: { data: { title: "Example title" } }
        },
        { provide: MatDialogRef, useClass: MatDialogRefMock }
      ]
    });

    fixture = TestBed.createComponent(LoginDialogComponent);
    component = fixture.componentInstance;
    store = TestBed.get(Store);
    spyOn(store, "dispatch").and.callThrough();
    fixture.detectChanges();
  });

  it("should be created", () => {
    expect(component).toBeTruthy();
  });
});
