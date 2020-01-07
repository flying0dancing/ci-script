import { Dataset, DatasetStatus } from "@/core/api/staging/staging.interfaces";
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { environment } from "@env/environment";

@Component({
  selector: "app-staging-job-dialog-dataset",
  templateUrl: "./staging-job-dialog-dataset.component.html",
  styleUrls: ["./staging-job-dialog-dataset.component.scss"]
})
export class StagingJobDialogDatasetComponent implements OnInit {
  public datasetStatus = DatasetStatus;

  @Input() dataset: Dataset;
  @Input() viewJobDetails: boolean;

  @Output() viewJobDetailsEvent: EventEmitter<Dataset> = new EventEmitter();

  public icon;
  public iconClass;
  public environment = environment;

  ngOnInit() {
    this.statusRenderer(this.dataset.status);
  }

  validationFailed(dataset: Dataset) {
    return dataset.status === DatasetStatus.VALIDATION_FAILED;
  }

  public handleClickEvent(dataset) {
    this.viewJobDetailsEvent.emit(dataset);
  }

  private statusRenderer(value) {
    switch (value) {
      case DatasetStatus.UPLOADING:
        this.icon = "warning";
        this.iconClass = "warning";
        break;
      case DatasetStatus.VALIDATION_FAILED:
      case DatasetStatus.REGISTRATION_FAILED:
      case DatasetStatus.UPLOAD_FAILED:
        this.icon = "error";
        this.iconClass = "fail";
        break;
      default:
        this.icon = "check_circle";
        this.iconClass = "success";
    }
  }
}
