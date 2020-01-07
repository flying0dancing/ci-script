import {
  Component,
  Input,
  SimpleChanges,
  ChangeDetectionStrategy,
  OnChanges
} from '@angular/core';
import { OutputData } from '../interfaces/pipeline-step-test.interface';
import {
  Appearance,
  OUTPUT_DATA_STATUS_APPEARANCE_MAP,
  DATA_NOT_RUN_APPEARANCE
} from '../utilities/pipeline-step-tests.utilities';

@Component({
  selector: 'dv-pipeline-step-test-output-data-row-status-icon',
  templateUrl:
    './pipeline-step-test-output-data-row-status-icon.component.html',
  styleUrls: [
    './pipeline-step-test-output-data-row-status-icon.component.scss'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestOutputDataRowStatusIconComponent
  implements OnChanges {
  @Input() outputData: OutputData;

  status: Appearance;

  run: Appearance;

  ngOnChanges(changes: SimpleChanges) {
    if (changes.outputData) {
      this.status =
        this.outputData &&
        OUTPUT_DATA_STATUS_APPEARANCE_MAP[this.outputData.status];
      this.run =
        this.outputData && !this.outputData.run && DATA_NOT_RUN_APPEARANCE;
    }
  }
}
