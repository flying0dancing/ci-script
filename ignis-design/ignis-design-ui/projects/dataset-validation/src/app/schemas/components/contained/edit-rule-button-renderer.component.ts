import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ICellRendererParams } from 'ag-grid';
import { ICellRendererAngularComp } from 'ag-grid-angular';
import { RuleEventService } from '../../../schema-details/service/rule-event.service';

@Component({
  selector: 'dv-edit-rule-btn',
  templateUrl: './edit-field-rule-button-renderer.component.html',
  styleUrls: ['./edit-schema-button-renderer.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class EditRuleButtonRendererComponent
  implements ICellRendererAngularComp {
  id: number;
  cellValue: any;
  hover: boolean;

  constructor(private ruleEventService: RuleEventService) {}

  onClick(event) {
    event.preventDefault();
    this.ruleEventService.editEvent.emit({
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
