import { ChangeDetectionStrategy, Component, Input } from "@angular/core";

@Component({
  selector: "app-spinner",
  templateUrl: "./spinner.component.html",
  styleUrls: ["./spinner.component.scss"],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SpinnerComponent {
  @Input() text: string;

  @Input() set size(value: string) {
    this.sizes.indexOf(value) === -1
      ? console.warn("`size` value not valid")
      : (this.sizeClass = value);
  }

  sizeClass: string;

  private sizes = ["small", "medium"];
}
