import { dataset } from "@/core/api/staging/_tests/staging.mocks";
import { DatasetStatus } from "@/core/api/staging/staging.interfaces";
import { reducers } from "@/fcr/dashboard/_tests/dashboard-reducers.mock";
import { StagingJobDialogDatasetComponent } from "@/fcr/dashboard/jobs/staging/staging-job-dialog-dataset.component";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";
import { StoreModule } from "@ngrx/store";
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';

describe("StagingJobDialogDatasetComponent", () => {
  let component: StagingJobDialogDatasetComponent;
  let fixture: ComponentFixture<StagingJobDialogDatasetComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [StoreModule.forRoot(reducers), DateTimeModule],
      declarations: [StagingJobDialogDatasetComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(StagingJobDialogDatasetComponent);
    component = fixture.componentInstance;
    component.dataset = dataset;
    spyOn(<any>component, "statusRenderer").and.callThrough();
    fixture.detectChanges();
  }));

  describe("statusRenderer", () => {
    it("should set icon and iconClass to warning when status is uploading", () => {
      (<any>component).statusRenderer(DatasetStatus.UPLOADING);

      expect(component.icon).toBe("warning");
      expect(component.iconClass).toBe("warning");
    });

    it("should set icon to error and iconClass to fail when status is validation failed", () => {
      (<any>component).statusRenderer(DatasetStatus.VALIDATION_FAILED);

      expect(component.icon).toBe("error");
      expect(component.iconClass).toBe("fail");
    });

    it("should set icon to error and iconClass to fail when status is registration failed", () => {
      (<any>component).statusRenderer(DatasetStatus.REGISTRATION_FAILED);

      expect(component.icon).toBe("error");
      expect(component.iconClass).toBe("fail");
    });

    it("should set icon to error and iconClass to fail when status is upload failed", () => {
      (<any>component).statusRenderer(DatasetStatus.UPLOAD_FAILED);

      expect(component.icon).toBe("error");
      expect(component.iconClass).toBe("fail");
    });

    it("should set icon to check_circle and iconClass to success as the default status", () => {
      (<any>component).statusRenderer("");

      expect(component.icon).toBe("check_circle");
      expect(component.iconClass).toBe("success");
    });
  });

  describe("ngOnInit", () => {
    it("should call statusRenderer with the status of the dataset", () => {
      component.ngOnInit();

      expect((<any>component).statusRenderer).toHaveBeenCalled();
    });
  });
});
