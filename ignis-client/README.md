# Ignis Client
This module is used to provide Java APIs for submitting REST requests to the different FCR Engine applications.

The REST library chosen for these modules is [Retrofit](https://square.github.io/retrofit/).

## Functional Tests
These client modules are used to drive acceptance tests in the module [`ignis-functional-test`](../ignis-functional-test/pom.xml)

## Sub Modules
- `ignis-client-core` - Shared code for the different clients, contains interceptors, config classes etc
- `ignis-client-design` - Client for the Design Studio application, mainly used by functional tests
- `ignis-client-external` - Client used by AgileReporter and automated process at client site (to automate running of FCR Jobs)
- `ignis-client-internal` -  Client used by the Spark Jobs to communicate back to `ignis-server`