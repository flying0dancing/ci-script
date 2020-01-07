import { ExitStatus, JobStatus } from '@/core/api/staging/staging.interfaces';
import { Component } from '@angular/core';
import { ICellRendererAngularComp } from 'ag-grid-angular';

@Component({
  selector: "app-status-renderer",
  templateUrl: "./status.renderer.html",
  styleUrls: ["./status.renderer.scss"]
})
export class StatusRendererComponent implements ICellRendererAngularComp {
  params: any;
  tooltip: string;
  icon;
  iconClass;

  private static statusCellRenderer(
    value: JobStatus,
    exitStatus: ExitStatus
  ): [string, string] {
    switch (value) {
      case JobStatus.STOPPED:
        return ["warning", "warning"];
      case JobStatus.ABANDONED:
        return ["warning", "warning"];
      case JobStatus.UNKNOWN:
        return ["warning", "warning"];
      case JobStatus.STOPPING:
        return ["warning", "warning"];
      case JobStatus.FAILED:
        return ["error", "fail"];
      case JobStatus.COMPLETED:
        if (exitStatus === ExitStatus.COMPLETED) {
          return ["check_circle", "success"];
        } else {
          return ["error", "fail"];
        }
      default:
        return [null, null];
    }
  }

  refresh(): boolean {
    return false;
  }

  agInit(params: any) {
    if (params.data !== undefined) {
      this.params = params;
      const jobStatus = params.value;
      const exitStatus = params.data.exitCode;
      this.tooltip =
        jobStatus === JobStatus.COMPLETED && exitStatus !== ExitStatus.COMPLETED
          ? JobStatus.FAILED
          : jobStatus;
      const [icon, iconClass] = StatusRendererComponent.statusCellRenderer(
        jobStatus,
        exitStatus
      );
      this.icon = icon;
      this.iconClass = iconClass;
    }
  }
}
