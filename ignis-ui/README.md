# Fcr Module

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 1.5.0.

## Dependencies

What you need to run this app:

* [Node](https://nodejs.org/) version >= 5.0 and [NPM](https://www.npmjs.com/) >= 5
* Install @angular/cli globally using `npm install -g @angular/cli`

## NPM Commands

### Development server

Run `npm start -- --app app-name` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

This command will also startup the Mocks server and proxy requests to `http://localhost:4200/mocks`.

### Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

### Production server

Run `npm run start:prod` for a production server. Navigate to `http://localhost:4201/`.

This command will startup the Mocks server and proxy requests to `http://localhost:4201/mocks`.

### Mocks server

Run `npm run mocks` for a mocks server. Navigate to `http://localhost:3000/`.

### Build

Run `npm run build -- --app app-name` to build a project. The build artifacts will be stored in the `dist/` directory.

Run `npm run build:prod -- --app app-name` for a production build.

### Lint

Run `npm run lint` to lint TypeScript files.

### Running unit tests

Run `npm test -- --app app-name` to execute the unit tests via [Karma](https://karma-runner.github.io). Use the
`--watch` flag to watch for file changes. Code coverage reports will be stored in the `coverage/` directory.

Run `npm run test:ci -- --app app-name` for continuous integration environments.

### Running end-to-end tests

Run `npm run e2e -- --app app-name` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).
Prior to tests being executed, it will build and serve the app in production mode.

Run `npm run e2e:ci -- --app app-name` for continuous integration environments.

### Bundle Fcr

Run `npm run bundle-fcr` to generate an interactive static HTML page to analyse bundle content.
The interactive HTML page will be stored in the `report/bundle-fcr` directory.

NOTE: Prior to running this command, you must generate a `stats.json` file using 
the production build command with the adition of the `--stats-json` flag (`npm run build:prod -- --app app-name --stats-json`).

### Prepush

Before pushing code to Bitbucket, run `npm run prepush -- --app app-name`. This will run the `lint`, `test` and `e2e` commands sequentially.

Run `npm run prepush:ci -- --app app-name` for continuous integration environments.

## Coding Concepts / Principles

We try to follow a lot of the concepts from the [Angular Style Guide](https://angular.io/guide/styleguide). Most of the principles are enforced using [Codelyzer](http://codelyzer.com/).

The main ones to keep in mind are:

* Follow [LIFT](https://angular.io/guide/styleguide#lift) principle when organising files / folders.
* Organize [folders by feature](https://angular.io/guide/styleguide#folders-by-feature-structure), not by file type.

### NGRX / Redux Concepts

The main ones to keep in mind are:

* Only use a reducer when more than one area of an application needs to know about its data.
* Try to [re-use reducer logic](https://github.com/reactjs/redux/issues/2520#issuecomment-315797753) where possible.
* Use [Selectors](https://github.com/ngrx/platform/blob/master/docs/store/selectors.md) to access slices of store state.

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).
