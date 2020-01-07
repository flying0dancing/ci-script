import { AbstractControl } from '@angular/forms';
import { PageEvent } from '@angular/material';
import { Field } from '../../../../schemas';

export class PipelineSelectsService {

  selectsControls: AbstractControl[] = [];

  length = 0;
  pageSize = 3;
  currentPage = 0;
  pagedSelectsControls: AbstractControl[] = [];

  currentFilter = '';
  onlyShowErrors = false;

  outputFieldIdMap: { [key: string]: Field } = {};

  constructor(selectsControls: AbstractControl[],
              private fields: Field[],
              public fieldsWithErrors: { [id: number]: any }) {

    this.selectsControls = selectsControls.sort((a, b) => a.value.order > b.value.order ? 1 : -1);
    fields.forEach(field => this.outputFieldIdMap[field.id] = field);
    this.length = fields.length;

  }

  onPage($event: PageEvent) {
    this.currentPage = $event.pageIndex;
    this.pageSize = $event.pageSize;

    this.onFilter(this.currentFilter);
  }

  onOnlyShowErrors(onlyShowErrors: boolean) {
    this.onlyShowErrors = onlyShowErrors;
    this.onFilter(this.currentFilter);
  }

  onFilter(outputFieldName: string) {
    const filtered = this.filterErrors(
      this.filterSelects(outputFieldName));
    this.currentFilter = outputFieldName;

    const start = this.currentPage * this.pageSize;
    const end = Math.min(start + this.pageSize, filtered.length);
    this.length = filtered.length;
    this.pagedSelectsControls = filtered.slice(start, end);
  }

  private filterSelects(outputFieldName: string) {
    if (!outputFieldName || outputFieldName.length === 0) {
      return this.selectsControls;
    }

    const matchRegex = new RegExp(outputFieldName, 'i');

    return this.selectsControls
      .filter(control => this.outputFieldIdMap[control.value.outputFieldId].name.match(matchRegex));
  }

  private filterErrors(filtered: AbstractControl[]): AbstractControl[] {
    if (this.onlyShowErrors && Object.keys(this.fieldsWithErrors).length > 0) {
      return filtered.filter(control => !!this.fieldsWithErrors[control.value.outputFieldId]);
    }

    return filtered;
  }

  getOutputField(id: number) {
    return this.outputFieldIdMap[id];
  }

  reset() {
    this.currentFilter = '';
    this.currentPage = 0;
    this.onPage({
      length: this.fields.length,
      previousPageIndex: null,
      pageIndex: 0,
      pageSize: 3
    });
  }

  getPagedSelectsControls() {
    return this.pagedSelectsControls;
  }

  reOrder() {
    this.selectsControls = this.selectsControls.sort((a, b) => a.value.order > b.value.order ? 1 : -1);
    this.onPage({ length: this.length, pageSize: this.pageSize, pageIndex: this.currentPage, previousPageIndex: 0 });
  }
}
