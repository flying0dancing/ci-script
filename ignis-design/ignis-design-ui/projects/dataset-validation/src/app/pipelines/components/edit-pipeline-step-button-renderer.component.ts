import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Output
} from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { PipelineStepEventService } from '../services/pipeline-step-event.service';

@Component({
  selector: 'dv-edit-pipeline-step-btn',
  templateUrl: './edit-pipeline-step-button-renderer.component.html',
  styleUrls: [
    '../../schemas/components/contained/edit-schema-button-renderer.component.scss'
  ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditPipelineStepButtonRendererComponent
  implements ICellRendererAngularComp {
  @Output() editEvent = new EventEmitter<number>();

  id: number;
  cellValue: any;
  hover: boolean;

  constructor(private stepEventService: PipelineStepEventService) {}

  onClick(event) {
    event.preventDefault();
    this.stepEventService.editEvent.emit({
      id: this.id
    });
  }

  agInit(params: ICellRendererParams): void {
    this.id = params.data.id;
    this.cellValue = params.getValue();
  }

  refresh(): boolean {
    return false;
  }
}
