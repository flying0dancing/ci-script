import { Component } from "@angular/core";
import { Store } from "@ngrx/store";
import { Observable } from "rxjs";

import { selectHasPendingHttpRequests, State } from "./reducers";

@Component({
  selector: "app-progress-bar",
  templateUrl: "./progress-bar.component.html",
  styleUrls: ["./progress-bar.component.scss"]
})
export class ProgressBarComponent {
  visible$: Observable<boolean>;

  constructor(private store: Store<State>) {
    this.visible$ = store.select(selectHasPendingHttpRequests);
  }
}
