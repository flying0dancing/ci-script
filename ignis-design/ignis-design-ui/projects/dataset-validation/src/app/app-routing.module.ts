import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { PipelineDetailsContainerComponent } from './pipelines/components/pipeline-details-container.component';
import { ProductConfigContainerComponent } from './product-configs/components/product-config-container.component';
import { ProductConfigsContainerComponent } from './product-configs/components/product-configs-container.component';
import { PipelineStepTestPageContainerComponent } from './pipeline-step-tests/components/pipeline-step-test-page-container.component';
import { SchemaDetailsContainerComponent } from './schema-details/components/schema-details-container.component';

const routes: Routes = [
  {
    path: 'product-configs',
    component: ProductConfigsContainerComponent
  },
  {
    path: 'product-configs/:id',
    component: ProductConfigContainerComponent
  },
  {
    path: 'product-configs/:productId/schemas/:id',
    component: SchemaDetailsContainerComponent
  },
  {
    path: 'product-configs/:productId/pipelines/:id',
    component: PipelineDetailsContainerComponent
  },
  {
    path: 'product-configs/:productId/pipelines/:pipelineId/stepTests/:id',
    component: PipelineStepTestPageContainerComponent
  },
  {
    path: '**',
    redirectTo: 'product-configs',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
