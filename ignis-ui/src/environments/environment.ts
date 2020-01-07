// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const host = "https://localhost:8443/fcrengine";

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
