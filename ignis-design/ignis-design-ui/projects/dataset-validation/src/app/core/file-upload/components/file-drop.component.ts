import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges
} from '@angular/core';
import * as ArrayUtilities from '../../utilities/array.utilities';

@Component({
  selector: 'dv-file-drop',
  templateUrl: './file-drop.component.html',
  styleUrls: ['./file-drop.component.scss']
})
export class FileDropComponent implements OnChanges {
  allowedFileTypesText: string;

  @Input() allowedFileTypes: string[];
  @Output() selectFilesClick: EventEmitter<undefined> = new EventEmitter();

  private _allowedFileTypes: string[];

  ngOnChanges(changes: SimpleChanges) {
    if (changes.allowedFileTypes) {
      this.allowedFileTypesText = this.formatAllowedFileTypesText(
        this.allowedFileTypes
      );
    }
  }

  handleSelectFilesClick(e: Event) {
    e.preventDefault();

    this.selectFilesClick.emit();
  }

  formatAllowedFileTypesText(allowedFileTypes: string[]): string {
    return ArrayUtilities.isArray(allowedFileTypes)
      ? 'You can upload' +
          (allowedFileTypes.length === 1
            ? ` ${allowedFileTypes[0]} files`
            : allowedFileTypes.reduce(
                (accumulator, currentValue, index) =>
                  index === allowedFileTypes.length - 1
                    ? `${accumulator} or ${currentValue} files`
                    : `${accumulator} ${currentValue},`,
                ''
              ))
      : '';
  }
}
