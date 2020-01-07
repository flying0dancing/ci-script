export const host = "/fcrengine";

export const environment = {
  production: true,
  storeLogging: false,
  api: {
    root: `${host}/api`,
    internalRoot: `${host}/api/internal`,
    externalRoot: `${host}/api/v1`,
    externalRootV2: `${host}/api/v2`
  },
  url: {
    login: `${host}/login`
  }
};
