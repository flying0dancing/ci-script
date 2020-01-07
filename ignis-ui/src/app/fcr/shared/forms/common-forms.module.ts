import { NAMESPACE } from '@/core/api/auth/auth.constants';
import { AuthEffects } from '@/core/api/auth/auth.effects';
import { reducer } from '@/core/api/auth/auth.reducer';
import { AutoCompleteFormComponent } from '@/fcr/shared/forms/auto-complete-form.component';
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule, MatFormFieldModule, MatSelectModule, MatTooltipModule } from '@angular/material';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { EffectsModule } from '@ngrx/effects';
import { StoreModule } from '@ngrx/store';

@NgModule({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatInputModule,
    MatButtonModule,
    MatAutocompleteModule,
    MatSelectModule,
    MatTooltipModule,
    MatInputModule,
    MatFormFieldModule,
    StoreModule.forFeature(NAMESPACE, reducer),
    EffectsModule.forFeature([AuthEffects])
  ],
  exports: [AutoCompleteFormComponent],
  declarations: [AutoCompleteFormComponent]
})
export class CommonFormsModule {}
