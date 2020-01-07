import {
  Component,
  Input,
  SimpleChanges,
  ChangeDetectionStrategy,
  OnChanges
} from '@angular/core';
import { PipelineStepTestStatus } from '../interfaces/pipeline-step-test.interface';

interface Icon {
  name: string;
  color: string;
  displayText: string;
}

@Component({
  selector: 'dv-pipeline-step-test-status-icon',
  templateUrl: './pipeline-step-test-status-icon.component.html',
  styleUrls: ['./pipeline-step-test-status-icon.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PipelineStepTestStatusIconComponent implements OnChanges {
  @Input() status: PipelineStepTestStatus;

  icon: Icon;

  private readonly iconMap: {
    [key: string]: Icon;
  } = {
    [PipelineStepTestStatus.Pass]: {
      name: 'done',
      color: 'green',
      displayText: 'Pass'
    },
    [PipelineStepTestStatus.Fail]: {
      name: 'error',
      color: 'red',
      displayText: 'Fail'
    },
    [PipelineStepTestStatus.Pending]: {
      name: 'timer',
      color: 'initial',
      displayText: 'Pending'
    }
  };

  ngOnChanges(changes: SimpleChanges) {
    if (changes.status) {
      this.icon = this.iconMap[changes.status.currentValue];
    }
  }
}
