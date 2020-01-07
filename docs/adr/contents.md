# Architectural Decision Records
This page is used to house all `major` ADRs. These provide a logs of all decisions made during the development of this 
project along with 
- Why the decision was required (context)
- Why the decision was made (decision)
- The consequences of that decision

The intention being to provide a historical log for new joiners to read to understand the architecture and code structure
of the project.


## Contents
- [Adopting Functional Approach With io.vavr](2018-05-22-adopt-functional-approach-with-vavr.md)
- [Upgrading dependencies (Spring Boot, Jackson and more)](2018-06-19-upgrade-spring-jackson-mockito.md)
- [Phoenix Query Server Load Balancer](2018-06-27-phoenix-query-server-load-balancer.md)
- [Ignis Design Studio](2018-07-11-ignis-design-studio.md)
- [Split Installer](2018-12-10-split-installer.md)


## Creating an ADR
A good template to follow when creating an Architectural Decision Record is given below
```
# Title

## Status
ACCEPTED/IN REVIEW/DECLINED etc.. (declined might be a very important status historically!)

## Context
- Why this was a necessary decision?
- Why was it worth recording?

## Decision
- Why was the decision made?
- What were the relevant options/factors

## Consequences
- What were the consequences of that decision
    - For the code base
    - For the project
    - For the client
    - etc...
```