import {
  Environment,
  INTERNAL_API_PATH,
  ROOT_API_PATH
} from './environment.interface';

export const ENVIRONMENT: Environment = {
  production: true,
  api: {
    root: ROOT_API_PATH,
    internal: INTERNAL_API_PATH
  }
};
