# Ignis
This repository houses all the code for the FCR Engine and FCR Design Studio.
FCR Engine, AKA "Calc Engine", is a tool used to transform and validate regulatory data.

[TOC]

#### Agile Reporter
FCR Engine sits in the Agile Reporter "suite of products" and Agile Reporter integrates with FCR 
Engine using both a REST Api and JDBC to read data from FCR's Big Data Database [Apache Phoenix](https://phoenix.apache.org)

## Docs / ADRs
The docs for this project can be found [here](docs/contents.md).

## Tools
- JDK 8
- Maven 3+
- GitBash
- IntelliJ IDEA (with Lombok Pluging)
- Jenkins 2

## Projects
The FCR Engine project can be found in Jira under the project [ARCE](https://jira.vermeg.com/projects/ARCE/)
The wiki space can be found here [AgileREPORTER - FCR Engine](https://wiki.vermeg.com/display/ARCE/AgileREPORTER+-+FCR+Engine)

### Jenkins
The project is built using the following Jenkins jobs
- [ignis](https://jenkins-b.lombardrisk.com/view/Ignis/job/ignis/) - The main master build, automatic build
- [ignis-pr](https://jenkins-b.lombardrisk.com/view/Ignis/job/ignis-pr/) - PR job that runs unit and integration tests - automatic build
- [Release To Environments](https://jenkins-b.lombardrisk.com/view/Ignis/job/env-release/) - Deploy and release candidate releases of FCR Engine and/or AgileREPORTER to QA environments
- [ignis-performance](https://jenkins-b.lombardrisk.com/view/Ignis/job/ignis-performance/) - Runs performance jobs nightly

### Sonar Profile
Sonar stats can be found [here](http://sonar.lombardrisk.com/overview?id=com.lombardrisk%3Aignis)

## Running the build
This project uses maven as a build tool, the normal build can be run using the command
```bash
mvn clean install
```

For development purposes it is recommended that the following script be run
```bash
$ ./install-server-and-spark.sh
```
Which compiles and tests all the modules needed to run the applications, it skips building the Functional Tests and the 
Installer. The script can be run with other maven cmd line params i.e.
```bash
$ ./install-server-and-spark.sh -P skipTests
$ ./install-server-and-spark.sh -rf :ignis-server
```

## Module Structure
This repository is split into different modules to house different applications and code.

- `ignis-server` - Houses code for the FCR Engine application server
- `ignis-design` - Houses code for the FCR Design Studio application
- [`ignis-api`](ignis-api/README.md) - Is used to share code between FCR Engine and Design Studio
- `ignis-client` -  This module is used to provide a java client to drive the FCR Engine and Design Studio,
this client module is used by AgileReporter
- `ignis-functional-test` - This contains acceptance tests for both applications and uses the `ignis-client` code to
drive functionality via the REST API
- `ignis-ui` -  Angular code for the FCR Engine front end ui
- `ignis-core` - Common code used by FCR Engine, Design Studio and Spark Jobs
- `ignis-spark` - Code for running Spark jobs that insert and transform data in FCR Engine
- [`ignis-server-installer`](ignis-server-installer/README.md) - FCR Engine ignis-server installer
- [`ignis-platform-tools-installer`](ignis-platform-tools-installer/README.md) - FCR Engine platform-tools installer for on-premise releases

//TODO - Add more info