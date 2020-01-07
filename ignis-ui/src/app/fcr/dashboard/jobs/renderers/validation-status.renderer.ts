import { ValidationStatus } from "@/core/api/datasets/datasets.interfaces";
import { Component } from "@angular/core";
import { ICellRendererAngularComp } from "ag-grid-angular";

@Component({
  selector: "app-validation-status-renderer",
  templateUrl: "./validation-status.renderer.html",
  styleUrls: ["./validation-status.renderer.scss"]
})
export class ValidationStatusRendererComponent
  implements ICellRendererAngularComp {
  params: any;
  icon;
  iconClass;
  jobRunning = false;
  hasRules: boolean;

  refresh(params: any): boolean {
    return false;
  }

  agInit(params: any) {
    this.params = params;
    this.hasRules = params.data.hasRules;

    if (this.hasRules) {
      this.statusCellRenderer(params.value);
    }
  }

  private statusCellRenderer(value) {
    switch (value) {
      case ValidationStatus.VALIDATING:
        this.jobRunning = true;
        break;
      case ValidationStatus.VALIDATED:
        this.icon = "check_circle";
        this.iconClass = "success";
        break;
      case ValidationStatus.QUEUED:
        this.icon = "access_time";
        break;
      case ValidationStatus.VALIDATION_FAILED:
        this.icon = "error";
        this.iconClass = "fail";
        break;
      default:
        this.icon = "";
    }
  }
}
