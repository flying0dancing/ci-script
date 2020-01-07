import { IgnisFeature } from '@/core/api/features/features.enum';
import * as FeatureSelects from '@/core/api/features/features.selectors';
import { Table } from '@/core/api/tables/tables.interfaces';
import { ChangeDetectionStrategy, Component, Input, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { StageJobFormService } from './stage-job-form.service';

const MIN_SCHEMA_PERIOD_DATE: Date = new Date('1970-01-01');

@Component({
  selector: 'app-stage-job',
  templateUrl: './stage-job.component.html',
  styleUrls: ['./stage-job.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [StageJobFormService]
})
export class StageJobComponent implements OnInit {
  @Input() isUploading: boolean;

  appendFeatureActive$: Observable<boolean> = this.store
    .select(FeatureSelects.getByNameSelector(IgnisFeature.APPEND_DATASETS))
    .pipe(map(feature => feature.active));

  minDate = MIN_SCHEMA_PERIOD_DATE;
  schemaNameExtractor: (schema: Table) => string = schema => schema.displayName;

  constructor(
    public stageJobFormService: StageJobFormService,
    private store: Store<any>
  ) {
    this.stageJobFormService.init();
  }

  ngOnInit() {
  }

  public addItem(): void {
    this.stageJobFormService.addItem();
  }

  public removeItem(index): void {
    this.stageJobFormService.removeItem(index);
  }

  public submit(): void {
    this.stageJobFormService.submit();
  }

  sourceFileSelected(selectedSourceFile: string, itemControls) {
    itemControls.filePath.setValue(selectedSourceFile);
  }

  schemaSelected($event: Table, itemControls) {
    itemControls.selectedSchema.setValue($event);
    itemControls.schemaName.setValue($event.displayName);
  }
}
