import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { By } from "@angular/platform-browser";

import { SpinnerComponent } from "../spinner.component";

describe("SpinnerComponent", () => {
  const spinnerSelector = "mat-spinner";
  const textSelector = ".text";
  const componentText = "loading";

  let component: SpinnerComponent;
  let fixture: ComponentFixture<SpinnerComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [SpinnerComponent],
      schemas: [NO_ERRORS_SCHEMA]
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SpinnerComponent);
    component = fixture.componentInstance;
  });

  it("should add a `small` CSS class to the spinner and text elements", () => {
    const size = "small";

    component.size = size;
    component.text = componentText;

    fixture.detectChanges();

    const spinner = fixture.debugElement.query(By.css(spinnerSelector));
    const text = fixture.debugElement.query(By.css(textSelector));

    expect(spinner.nativeElement.classList).toContain(size);
    expect(text.nativeElement.classList).toContain(size);
  });

  it("should add a `medium` CSS class to the spinner and text elements", () => {
    const size = "medium";

    component.size = size;
    component.text = componentText;

    fixture.detectChanges();

    const spinner = fixture.debugElement.query(By.css(spinnerSelector));
    const text = fixture.debugElement.query(By.css(textSelector));

    expect(spinner.nativeElement.classList).toContain(size);
    expect(text.nativeElement.classList).toContain(size);
  });

  it("should log a warning to the console when the `size` property is not valid", () => {
    spyOn(console, "warn");

    component.size = "huge";

    fixture.detectChanges();

    expect(console.warn).toHaveBeenCalledTimes(1);
  });

  it("should not render the text element when the `text` property has no value", () => {
    fixture.detectChanges();

    const text = fixture.debugElement.query(By.css(textSelector));

    expect(text).toBeNull();
  });
});
