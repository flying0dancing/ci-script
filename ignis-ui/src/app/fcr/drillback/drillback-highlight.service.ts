import { EventEmitter, Injectable, Output } from "@angular/core";

@Injectable({ providedIn: "root" })
export class DrillbackHighlightService {
  @Output() public drillbackRowSelectEvent: EventEmitter<
    Map<string, Object>
  > = new EventEmitter();
}
