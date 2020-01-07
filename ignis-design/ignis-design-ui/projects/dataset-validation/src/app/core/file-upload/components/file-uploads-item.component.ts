import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'dv-file-uploads-item',
  templateUrl: './file-uploads-item.component.html',
  styleUrls: ['./file-uploads-item.component.scss']
})
export class FileUploadsItemComponent {
  @Input() fileName: string;

  @Input() fileSize: string;

  @Input() progress: number;

  @Input() showRemove = false;

  @Input() showRetry = false;

  @Input() removeTooltipText = 'Remove';

  @Input() retryTooltipText = 'Retry';

  @Input() showSuccessIcon = false;

  @Input() showFailIcon = false;

  @Input() showProgress = false;

  @Output() removeClick: EventEmitter<undefined> = new EventEmitter();

  @Output() retryClick: EventEmitter<undefined> = new EventEmitter();
}
