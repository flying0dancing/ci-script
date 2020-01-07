import { Inject, Injectable, InjectionToken } from '@angular/core';
import { Environment } from './environment.interface';

export const ENV_TOKEN = new InjectionToken<Environment>('environment');

@Injectable({ providedIn: 'root' })
export class EnvironmentService {
  constructor(@Inject(ENV_TOKEN) public readonly env: Environment) {}
}
