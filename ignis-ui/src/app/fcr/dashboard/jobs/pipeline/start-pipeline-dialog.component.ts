import { IgnisFeature } from '@/core/api/features/features.enum';
import * as FeatureSelects from '@/core/api/features/features.selectors';
import { StartPipelineService } from '@/fcr/dashboard/jobs/pipeline/start-pipeline.service';
import { LocaleTimePipe } from '@/fcr/shared/datetime/locale-time-pipe.component';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-start-pipeline-dialog',
  templateUrl: './start-pipeline-dialog.component.html',
  styleUrls: ['./stage-pipeline-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [StartPipelineService, LocaleTimePipe]
})
export class StartPipelineDialogComponent {
  runPipelineActive$: Observable<boolean> = this.store
    .select(FeatureSelects.getByNameSelector(IgnisFeature.RUN_PIPLINE_STEP
    ))
    .pipe(map(feature => feature.active));


  constructor(private store: Store<any>, public startPipelineService: StartPipelineService) {
    this.startPipelineService.init();
  }

  public autocompleteOptions(options: string): string[] {
    return StartPipelineService.toAutocompleteOptions(options);
  }

  public submit(): void {
    this.startPipelineService.submit();
  }

  isDatasetsEnabled() {
    return (this.startPipelineService.form.controls.selectedPipeline.valid &&
      this.startPipelineService.requiredSchemasReady$.subscribe(
        isRequiredSchemaReady => isRequiredSchemaReady));
  }
}
