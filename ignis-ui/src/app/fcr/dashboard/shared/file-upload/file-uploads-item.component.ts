import { Component, EventEmitter, Input, Output } from "@angular/core";

@Component({
  selector: "app-file-uploads-item",
  templateUrl: "./file-uploads-item.component.html",
  styleUrls: ["./file-uploads-item.component.scss"]
})
export class FileUploadsItemComponent {
  @Input() fileName: string;

  @Input() fileSize: string;

  @Input() progress: number;

  @Input() showRemove = false;

  @Input() showRetry = false;

  @Input() removeTooltipText = "Remove";

  @Input() retryTooltipText = "Retry";

  @Input() showSuccessIcon = false;

  @Input() showFailIcon = false;

  @Input() showProgress = false;

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onRemoveClick: EventEmitter<undefined> = new EventEmitter();

  // tslint:disable-next-line:no-output-on-prefix
  @Output() onRetryClick: EventEmitter<undefined> = new EventEmitter();
}
