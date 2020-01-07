import { NO_ERRORS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { By } from "@angular/platform-browser";
import { combineReducers, StoreModule } from "@ngrx/store";

import { ProgressBarComponent } from "../progress-bar.component";
import { reducers } from "../reducers";

function configureTestingModule(initialState) {
  TestBed.configureTestingModule({
    imports: [
      StoreModule.forRoot(
        { loaders: combineReducers(reducers) },
        { initialState }
      )
    ],
    declarations: [ProgressBarComponent],
    schemas: [NO_ERRORS_SCHEMA]
  });
}

describe("ProgressBarComponent", () => {
  const progressBarSelector = "mat-progress-bar";

  let fixture: ComponentFixture<ProgressBarComponent>;

  it("should not render the progress bar if no http requests are pending", () => {
    configureTestingModule({ loaders: { httpRequestCount: 0 } });

    fixture = TestBed.createComponent(ProgressBarComponent);

    fixture.detectChanges();

    const progressBar = fixture.debugElement.query(By.css(progressBarSelector));

    expect(progressBar).toBeNull();
  });

  it("should render the progress bar if http requests are pending", () => {
    configureTestingModule({ loaders: { httpRequestCount: 1 } });

    fixture = TestBed.createComponent(ProgressBarComponent);

    fixture.detectChanges();

    const progressBar = fixture.debugElement.query(By.css(progressBarSelector));

    expect(progressBar).toBeTruthy();
  });
});
