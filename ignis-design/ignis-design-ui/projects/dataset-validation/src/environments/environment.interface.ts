export const API_PATH = '/api';
export const ROOT_API_PATH = `${API_PATH}/v1`;
export const INTERNAL_API_PATH = `${API_PATH}/internal`;

export interface Environment {
  production: boolean;
  api: {
    internal: string;
    root: string;
    stub?: boolean;
  };
}
