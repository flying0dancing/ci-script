import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'dv-product-breadcrumb',
  templateUrl: './product-breadcrumb.component.html',
  styleUrls: ['./product-breadcrumb.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductBreadcrumbComponent {
  breadcrumbLevelEnum: typeof BreadcrumbLevel = BreadcrumbLevel;

  @Input() productId: number;
  @Input() productName: string;
  @Input() schemaDisplayName: string;
  @Input() pipelineId: number;
  @Input() pipelineName: string;
  @Input() pipelineStepTestName: string;
  @Input() breadcrumbLevel: BreadcrumbLevel;
}

export enum BreadcrumbLevel {
  Product = 'PRODUCT',
  Schema = 'SCHEMA',
  Pipeline = 'PIPELINE',
  PipelineStepTest = 'PIPELINE_STEP_TEST'
}
