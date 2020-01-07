import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { EnvironmentService } from '../../../../../environments/environment.service';

@Injectable({ providedIn: 'root' })
export class PipelineScriptletService {

  readonly scriptletsUrl: string;

  constructor(
    private http: HttpClient,
    private environmentService: EnvironmentService
  ) {
    this.scriptletsUrl = `${this.environmentService.env.api.root}/scriptlets/jars`;
  }

  getJarNames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.scriptletsUrl}`);
  }

  getClassNames(jarName: string): Observable<string[]> {
    return this.http.get<string[]>(`${this.scriptletsUrl}/${jarName}/classes`);
  }

  getMetadataInputs(jarName: string, className: string): Observable<string[]> {
    return this.http
      .get(`${this.scriptletsUrl}/${jarName}/classes/${className}/metadata?type=input`)
      .pipe(map(item => Object.keys(item)));
  }
}
