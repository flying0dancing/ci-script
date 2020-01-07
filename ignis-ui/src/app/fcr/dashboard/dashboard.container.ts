import * as StagingInterfaces from '@/core/api/staging/staging.interfaces';

import * as UsersSelectors from '@/core/api/users/users.selectors';
import * as JobsSelectors from '@/fcr/dashboard/jobs/jobs.selectors';
import { Component, OnInit } from '@angular/core';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { NAMESPACE } from './dashboard.constants';
import { fadeInFactory } from './shared/animations';

@Component({
  selector: "app-dashboard",
  templateUrl: "./dashboard.container.html",
  animations: [fadeInFactory(NAMESPACE)],
  styleUrls: ["./dashboard.container.scss"]
})
export class DashboardComponent implements OnInit {
  user$: Observable<any> = this.store.select(UsersSelectors.getCurrentUser);
  loadingUser$: Observable<any> = this.store.select(
    UsersSelectors.getCurrentUserLoading
  );
  runningJobs: number;

  constructor(private store: Store<any>) {}

  ngOnInit(): void {
    this.store
      .select(JobsSelectors.getStagingCollection)
      .pipe(
        map((jobs: StagingInterfaces.JobExecution[]) => {
          return jobs.filter(job => StagingInterfaces.isRunning(job));
        })
      )
      .subscribe(jobs => (this.runningJobs = jobs ? jobs.length : 0));
  }
}
