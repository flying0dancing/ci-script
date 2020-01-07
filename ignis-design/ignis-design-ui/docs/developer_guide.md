# Design Studio UI - Developer Guide

### Status - In progress

## Starting Design Studio Server
### Install all dependencies
Make sure to run the following command at the top level before proceeding (and after updating master)
```bash
mvn clean install -P skipTests
```

### Running the server
Run the following command from the root `ignis` directory, changing out the config location to be the location of the project on your machine.
```bash
mvn -f ignis-design/ignis-design-server/pom.xml spring-boot:run -Dspring.profiles.active="dev" -Dspring.config.location="C:/code/ignis/ignis-design/ignis-design-server/src/test/resources/"
```

### Migrating the database
Running the server will create a file based database that now needs to be migrated, to do this run.
```bash
./flyway-design-studio h2 migrate
```

### Running the ui
The Design ui project is found under `ignis/ignis-design/ignis-design-ui` and is an npm project that can be run locally using
```bash
./npm.sh run serve
```
