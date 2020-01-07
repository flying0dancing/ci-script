import { Component, Input } from "@angular/core";

@Component({
  selector: "app-dialog-header",
  templateUrl: "./dialog-header.component.html",
  styleUrls: ["./dialog-header.component.scss"]
})
export class DialogHeaderComponent {
  @Input() showClose = true;

  @Input() disableClose = false;
}
