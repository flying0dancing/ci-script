import { PipelinesService } from "@/core/api/pipelines/pipelines.service";
import { DateTimeModule } from '@/fcr/shared/datetime/datetime.module';
import { HttpClient } from "@angular/common/http";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ReactiveFormsModule } from "@angular/forms";
import { MatAutocompleteModule } from "@angular/material/autocomplete";
import { StoreModule } from "@ngrx/store";
import { reducers } from "../../_tests/dashboard-reducers.mock";
import { JobsHelperService } from "../jobs-helper.service";
import { StageJobFormService } from "../stage-job-form.service";
import { StageJobComponent } from "../stage-job.component";

describe("StageJobComponent", () => {
  let component: StageJobComponent;
  let fixture: ComponentFixture<StageJobComponent>;
  let http: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        MatAutocompleteModule,
        ReactiveFormsModule,
        StoreModule.forRoot(reducers),
        HttpClientTestingModule,
        DateTimeModule
      ],
      declarations: [StageJobComponent],
      schemas: [NO_ERRORS_SCHEMA],
      providers: [
        JobsHelperService,
        StageJobFormService,
        HttpClient,
        PipelinesService
      ]
    }).compileComponents();

    http = TestBed.get(HttpClient);
    fixture = TestBed.createComponent(StageJobComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  });

  describe("addItem", () => {
    it("should call the form service `addItem`", () => {
      spyOn(component.stageJobFormService, "addItem");

      component.addItem();

      expect(component.stageJobFormService.addItem).toHaveBeenCalledTimes(1);
    });
  });


  describe("removeItem", () => {
    it("should call the form service `addItem`", () => {
      spyOn(component.stageJobFormService, "removeItem");

      component.removeItem(2);

      expect(component.stageJobFormService.removeItem).toHaveBeenCalledTimes(1);
      expect(component.stageJobFormService.removeItem).toHaveBeenCalledWith(2);
    });
  });

  describe("submit", () => {
    it("should call the form service `addItem`", () => {
      spyOn(component.stageJobFormService, "submit");

      component.submit();

      expect(component.stageJobFormService.submit).toHaveBeenCalledTimes(1);
    });
  });
});
