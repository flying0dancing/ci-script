// export const host = 'https://localhost:8443/fcrengine';
export const host =
  "https://i-08bf7fed60e664856.internal.aws.lombardrisk.com:8443/fcrengine";

export const environment = {
  production: false,
  storeLogging: false,
  api: {
    root: `${host}/api`,
    internalRoot: `${host}/api/internal`,
    externalRoot: `${host}/api/v1`,
    externalRootV2: `${host}/api/v2`
  },
  url: {
    login: `/login`
  }
};
