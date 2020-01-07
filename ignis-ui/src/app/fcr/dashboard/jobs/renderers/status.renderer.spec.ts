import { JobStatus } from "@/core/api/staging/staging.interfaces";
import { StatusRendererComponent } from "@/fcr/dashboard/jobs/renderers/status.renderer";
import { NO_ERRORS_SCHEMA } from "@angular/core";
import { async, ComponentFixture, TestBed } from "@angular/core/testing";

describe("StatusRendererComponent", () => {
  let component: StatusRendererComponent;
  let fixture: ComponentFixture<StatusRendererComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [StatusRendererComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(StatusRendererComponent);
    component = fixture.componentInstance;

    fixture.detectChanges();
  }));

  describe("statusCellRenderer", () => {
    it("should set icon and iconClass to warning when status is stopped", () => {
      component.agInit({ value: JobStatus.STOPPED, data: {} });

      expect(component.icon).toBe("warning");
      expect(component.iconClass).toBe("warning");
    });

    it("should set icon and iconClass to warning when status is stopped", () => {
      component.agInit({ value: JobStatus.STOPPING, data: {} });

      expect(component.icon).toBe("warning");
      expect(component.iconClass).toBe("warning");
    });

    it("should set icon to error and iconClass to fail when status is failed", () => {
      component.agInit({ value: JobStatus.FAILED, data: {} });

      expect(component.icon).toBe("error");
      expect(component.iconClass).toBe("fail");
    });

    it("should set icon to check_circle and iconClass to success as the default status", () => {
      component.agInit({ data: {} });

      expect(component.icon).toBeNull();
      expect(component.iconClass).toBeNull();
    });
  });
});
